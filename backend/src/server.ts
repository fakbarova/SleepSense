import 'dotenv/config';
import Fastify from 'fastify';
import cors from '@fastify/cors';
import { z } from 'zod';
import crypto from 'node:crypto';
import {
  DailyInsightsInput,
  DailyInsightsOutput,
  SleepReportInput,
  SleepReportOutput,
  WeeklyReportInput,
  WeeklyReportOutput,
  getOpenAIClient,
} from './llm.js';
import { RouteSuggestInput, RouteSuggestOutput } from './routes.js';
import { extractBearerToken, UnauthorizedError, verifyOAuthToken } from './auth.js';
import { UserStore } from './user-store.js';
import { exchangeCodeForToken, refreshSpotifyToken, spotifyApiGet } from './spotify.js';

const ChallengeSyncSchema = z.object({
  id: z.string().min(1),
  title: z.string().min(1).max(120),
  category: z.string().min(1).max(40),
  durationDays: z.number().int().min(1).max(120),
  successCriteria: z.string().min(1).max(400),
  startEpochDay: z.number().int(),
  createdAtMs: z.number().int(),
  completedToday: z.boolean(),
});

const HabitSyncSchema = z.object({
  id: z.string().min(1),
  type: z.enum(['pre_sleep', 'morning']),
  title: z.string().min(1).max(120),
  reminderMinutesOfDay: z.number().int().min(0).max(24 * 60 - 1).nullable(),
  enabled: z.boolean(),
  completedToday: z.boolean(),
});

type AuthUser = Awaited<ReturnType<typeof verifyOAuthToken>>;
type CachedValue = { value: unknown; expiresAt: number };

const cacheTtlMs = Number(process.env.LLM_CACHE_TTL_MS ?? 5 * 60 * 1000);
const cacheMaxEntries = Number(process.env.LLM_CACHE_MAX_ENTRIES ?? 400);
const quotaWindowMs = Number(process.env.RATE_LIMIT_WINDOW_MS ?? 60_000);
const quotaMaxRequests = Number(process.env.RATE_LIMIT_MAX_PER_WINDOW ?? 45);
const profilePatchSchema = z.object({ name: z.string().trim().min(1).max(80).optional() });

const userStore = new UserStore();

const quotaMap = new Map<string, { count: number; resetAt: number }>();

const llmCache = new Map<string, CachedValue>();

function cacheKey(payload: unknown) {
  return crypto.createHash('sha256').update(JSON.stringify(payload)).digest('hex');
}

function getCached<T>(key: string): T | null {
  const entry = llmCache.get(key);
  if (!entry) return null;
  if (entry.expiresAt <= Date.now()) {
    llmCache.delete(key);
    return null;
  }
  return entry.value as T;
}

function setCached(key: string, value: unknown) {
  if (llmCache.size >= cacheMaxEntries) {
    const first = llmCache.keys().next().value;
    if (first) llmCache.delete(first);
  }
  llmCache.set(key, {
    value,
    expiresAt: Date.now() + cacheTtlMs,
  });
}

function enforceQuota(userId: string, route: string) {
  const key = `${userId}:${route}`;
  const now = Date.now();
  const current = quotaMap.get(key);
  if (!current || current.resetAt <= now) {
    quotaMap.set(key, { count: 1, resetAt: now + quotaWindowMs });
    return;
  }
  if (current.count >= quotaMaxRequests) {
    throw new Error('Rate limit exceeded');
  }
  current.count += 1;
  quotaMap.set(key, current);
}

function parseCorsOrigins() {
  const envValue = process.env.CORS_ORIGINS;
  if (!envValue) return false;
  const list = envValue
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
  return list.length > 0 ? list : false;
}

async function requireAuth(_authHeader: string | undefined): Promise<AuthUser> {
  // DEMO BYPASS — restore real auth before production.
  return {
    userId: 'demo-user',
    subject: 'demo',
    issuer: 'demo',
    email: 'demo@circadianx.local',
    name: 'Demo User',
  };
}

export function buildServer() {
  const app = Fastify({ logger: true, requestIdHeader: 'x-request-id' });

  app.register(cors, { origin: parseCorsOrigins() });
  app.addHook('onResponse', async (request, reply) => {
    request.log.info(
      { statusCode: reply.statusCode, method: request.method, url: request.url },
      'request_completed'
    );
  });

  app.setErrorHandler((error, req, reply) => {
    const err = error instanceof Error ? error : new Error('Unknown error');
    if (error instanceof UnauthorizedError) {
      return reply.status(401).send({ ok: false, error: error.message });
    }
    if (error instanceof z.ZodError) {
      return reply.status(400).send({
        ok: false,
        error: 'Validation failed',
        details: error.issues.map((issue) => ({
          path: issue.path.join('.'),
          message: issue.message,
        })),
      });
    }
    if (err.message === 'Rate limit exceeded') {
      return reply.status(429).send({ ok: false, error: err.message });
    }
    req.log.error({ err }, 'Unhandled request error');
    return reply.status(500).send({ ok: false, error: 'Internal server error' });
  });

  app.get('/health', async () => ({ ok: true }));

  app.post('/auth/verify', async (req, reply) => {
    const body = z.object({ idToken: z.string().min(1) }).parse(req.body);
    const auth = await verifyOAuthToken(body.idToken);
    const profile = await userStore.updateProfile(auth.userId, auth.name, auth.email);
    return reply.send({ ok: true, userId: auth.userId, email: auth.email, name: profile.name });
  });

  app.get('/users/me', async (req, reply) => {
    const auth = await requireAuth(req.headers.authorization);
    const user = await userStore.getUser(auth.userId);
    return {
      id: auth.userId,
      subject: auth.subject,
      issuer: auth.issuer,
      profile: user.profile,
      createdAt: user.updatedAt,
    };
  });

  app.patch('/users/me', async (req, reply) => {
    const auth = await requireAuth(req.headers.authorization);
    const body = profilePatchSchema.parse(req.body);
    const profile = await userStore.updateProfile(auth.userId, body.name ?? auth.name, auth.email);
    return reply.send({ ok: true, profile });
  });

  app.get('/sync/challenges', async (req, reply) => {
    const auth = await requireAuth(req.headers.authorization);
    const user = await userStore.getUser(auth.userId);
    return reply.send({ challenges: user.challenges });
  });

  app.put('/sync/challenges', async (req, reply) => {
    const auth = await requireAuth(req.headers.authorization);
    const body = z.object({ challenges: z.array(ChallengeSyncSchema) }).parse(req.body);
    const synced = await userStore.replaceChallenges(auth.userId, body.challenges);
    return reply.send({ ok: true, challenges: synced });
  });

  app.get('/sync/habits', async (req, reply) => {
    const auth = await requireAuth(req.headers.authorization);
    const user = await userStore.getUser(auth.userId);
    return reply.send({ habits: user.habits });
  });

  app.put('/sync/habits', async (req, reply) => {
    const auth = await requireAuth(req.headers.authorization);
    const body = z.object({ habits: z.array(HabitSyncSchema) }).parse(req.body);
    const synced = await userStore.replaceHabits(auth.userId, body.habits);
    return reply.send({ ok: true, habits: synced });
  });

  // --- Spotify OAuth (PKCE) ---
  const spotifyClientId = process.env.SPOTIFY_CLIENT_ID ?? '';
  const spotifyRedirectUri = process.env.SPOTIFY_REDIRECT_URI ?? 'sleepsense://spotify-auth';

  app.post('/spotify/exchange', async (req, reply) => {
    const auth = await requireAuth(req.headers.authorization);
    const body = z
      .object({
        code: z.string().min(1),
        codeVerifier: z.string().min(20),
        redirectUri: z.string().min(8).optional(),
      })
      .parse(req.body);

    if (!spotifyClientId) {
      return reply.status(500).send({ ok: false, error: 'Missing SPOTIFY_CLIENT_ID' });
    }

    const token = await exchangeCodeForToken({
      clientId: spotifyClientId,
      redirectUri: body.redirectUri ?? spotifyRedirectUri,
      code: body.code,
      codeVerifier: body.codeVerifier,
    });

    const expiresAtMs = Date.now() + token.expires_in * 1000;
    await userStore.setSpotifyToken(auth.userId, {
      accessToken: token.access_token,
      refreshToken: token.refresh_token ?? null,
      expiresAtMs,
      scope: token.scope ?? null,
    });

    return reply.send({ ok: true, expiresAtMs });
  });

  app.get('/spotify/me', async (req, reply) => {
    const auth = await requireAuth(req.headers.authorization);
    const user = await userStore.getUser(auth.userId);
    if (!user.spotify) return reply.status(400).send({ ok: false, error: 'Spotify not connected' });
    if (!spotifyClientId) return reply.status(500).send({ ok: false, error: 'Missing SPOTIFY_CLIENT_ID' });

    let accessToken = user.spotify.accessToken;
    if (user.spotify.expiresAtMs <= Date.now() + 30_000) {
      if (!user.spotify.refreshToken) {
        return reply.status(401).send({ ok: false, error: 'Spotify session expired' });
      }
      const refreshed = await refreshSpotifyToken({
        clientId: spotifyClientId,
        refreshToken: user.spotify.refreshToken,
      });
      const expiresAtMs = Date.now() + refreshed.expires_in * 1000;
      accessToken = refreshed.access_token;
      await userStore.setSpotifyToken(auth.userId, {
        accessToken,
        refreshToken: refreshed.refresh_token ?? user.spotify.refreshToken,
        expiresAtMs,
        scope: refreshed.scope ?? user.spotify.scope,
      });
    }

    const profile = await spotifyApiGet(
      accessToken,
      'https://api.spotify.com/v1/me',
      z.object({ id: z.string(), display_name: z.string().nullable().optional() })
    );
    return reply.send({ ok: true, profile });
  });

  app.get('/spotify/open/winddown', async (req, reply) => {
    // Minimal “feature”: return a Spotify deep link the app can open.
    // You can replace this with a user-customizable playlist later.
    const _auth = await requireAuth(req.headers.authorization);
    const playlistId = process.env.SPOTIFY_WINDDOWN_PLAYLIST_ID ?? '';
    if (!playlistId) {
      return reply
        .status(500)
        .send({ ok: false, error: 'Missing SPOTIFY_WINDDOWN_PLAYLIST_ID' });
    }
    return reply.send({
      ok: true,
      uri: `spotify:playlist:${playlistId}`,
      url: `https://open.spotify.com/playlist/${playlistId}`,
    });
  });

  // --- LLM-Powered Endpoints ---

  app.post('/insights/daily', async (req) => {
    const auth = await requireAuth(req.headers.authorization);
    enforceQuota(auth.userId, 'insights-daily');
  const input = DailyInsightsInput.parse(req.body);
  const key = `daily:${cacheKey(input)}`;
    const cached = getCached<unknown>(key);
    if (cached) return DailyInsightsOutput.parse(cached);

  const client = getOpenAIClient();
  const system = [
    'You are SleepSense, a privacy-preserving habit and sleep coach.',
    'Return concise, non-medical, non-judgmental suggestions.',
    'Never claim diagnosis. Avoid sensitive inferences.',
    'Output must be valid JSON for the given schema.',
  ].join(' ');

  const user = {
    task: 'Generate 1-3 actionable micro-suggestions for today.',
    input,
    outputSchema: DailyInsightsOutput.shape,
  };

  const model = process.env.OPENAI_MODEL ?? 'gpt-4.1-mini';
  const resp = await client.chat.completions.create({
    model,
    temperature: 0.4,
    response_format: { type: 'json_object' },
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: JSON.stringify(user) },
    ],
  });

  const text = resp.choices[0]?.message?.content ?? '{}';
    const parsed = DailyInsightsOutput.parse(JSON.parse(text));
    setCached(key, parsed);
    return parsed;
  });

  app.post('/insights/weekly', async (req) => {
    const auth = await requireAuth(req.headers.authorization);
    enforceQuota(auth.userId, 'insights-weekly');
    const input = WeeklyReportInput.parse(req.body);
    const key = `weekly:${cacheKey(input)}`;
    const cached = getCached<unknown>(key);
    if (cached) return WeeklyReportOutput.parse(cached);

  const client = getOpenAIClient();
  const system = [
    'You are SleepSense, a privacy-preserving habit and sleep coach.',
    'Write a short weekly report: one headline, one key observation, one action for next week.',
    'Be specific to the metrics. Avoid any medical claims.',
    'Output must be valid JSON for the given schema.',
  ].join(' ');

  const user = {
    task: 'Generate a weekly report.',
    input,
    outputSchema: WeeklyReportOutput.shape,
  };

  const model = process.env.OPENAI_MODEL ?? 'gpt-4.1-mini';
  const resp = await client.chat.completions.create({
    model,
    temperature: 0.4,
    response_format: { type: 'json_object' },
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: JSON.stringify(user) },
    ],
  });

  const text = resp.choices[0]?.message?.content ?? '{}';
    const parsed = WeeklyReportOutput.parse(JSON.parse(text));
    setCached(key, parsed);
    return parsed;
  });

  app.post('/report', async (req) => {
    const auth = await requireAuth(req.headers.authorization);
    enforceQuota(auth.userId, 'report');
    const input = SleepReportInput.parse(req.body);
    const key = `report:${cacheKey(input)}`;
    const cached = getCached<unknown>(key);
    if (cached) return SleepReportOutput.parse(cached);

  const client = getOpenAIClient();
  const system = [
    'You are a sleep health analyst for SleepSense.',
    'Given the user sleep, step, and goal data, generate a structured JSON report.',
    'Be specific, encouraging, and practical. Avoid diagnosis or urgent medical claims.',
    'Risk assessment may discuss fragmentation patterns, but must say it is not a medical diagnosis.',
    'Output must be valid JSON for the given schema.',
  ].join(' ');

  const user = {
    task: 'Generate a demo-ready weekly sleep report.',
    input,
    outputSchema: SleepReportOutput.shape,
  };

  const model = process.env.OPENAI_MODEL ?? 'gpt-4.1-mini';
  const resp = await client.chat.completions.create({
    model,
    temperature: 0.35,
    response_format: { type: 'json_object' },
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: JSON.stringify(user) },
    ],
  });

  const text = resp.choices[0]?.message?.content ?? '{}';
    const parsed = SleepReportOutput.parse(JSON.parse(text));
    setCached(key, parsed);
    return parsed;
  });

  app.post('/routes/suggest', async (req, reply) => {
    const auth = await requireAuth(req.headers.authorization);
    enforceQuota(auth.userId, 'routes-suggest');
    const input = RouteSuggestInput.parse(req.body);
    const key = `route:${cacheKey(input)}`;
    const cached = getCached<unknown>(key);
    if (cached) return RouteSuggestOutput.parse(cached);

    const apiKey = process.env.GOOGLE_ROUTES_API_KEY;
    if (!apiKey) {
      return reply
        .status(500)
        .send({ ok: false, error: 'Missing GOOGLE_ROUTES_API_KEY' });
    }

    const routesResp = await fetch(
      'https://routes.googleapis.com/directions/v2:computeRoutes',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Goog-Api-Key': apiKey,
          'X-Goog-FieldMask':
            'routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline',
        },
        body: JSON.stringify({
          origin: {
            location: { latLng: { latitude: input.origin.lat, longitude: input.origin.lng } },
          },
          destination: {
            location: {
              latLng: { latitude: input.destination.lat, longitude: input.destination.lng },
            },
          },
          travelMode: 'WALK',
          routingPreference: 'TRAFFIC_UNAWARE',
          computeAlternativeRoutes: false,
          polylineEncoding: 'ENCODED_POLYLINE',
        }),
      }
    );

    if (!routesResp.ok) {
      const raw = await routesResp.text();
      return reply
        .status(502)
        .send({ ok: false, error: `Google Routes error: ${routesResp.status} ${raw}` });
    }

    const json = (await routesResp.json()) as {
      routes?: Array<{
        distanceMeters?: number;
        duration?: string;
        polyline?: { encodedPolyline?: string };
      }>;
    };
    const first = json.routes?.[0];
    const distanceMeters = Math.max(0, first?.distanceMeters ?? 0);
    const durationSeconds = Math.max(
      0,
      Number(String(first?.duration ?? '0s').replace('s', '')) || 0
    );
    const encodedPolyline = first?.polyline?.encodedPolyline ?? '';
    if (!encodedPolyline) {
      return reply.status(502).send({ ok: false, error: 'No route polyline returned' });
    }

    const metersPerStep = 0.78;
    const routeSteps = Math.round(distanceMeters / metersPerStep);
    const extraMinutes = Math.max(1, Math.round(durationSeconds / 60));

    const out = RouteSuggestOutput.parse({
      suggestion: {
        summary: `Planned walk: ~${routeSteps.toLocaleString()} steps (${extraMinutes} min).`,
        extraSteps: routeSteps,
        extraMinutes,
        distanceMeters,
        durationSeconds,
        encodedPolyline,
      },
    });
    setCached(key, out);
    return out;
  });

  app.post('/chat', async (req) => {
    const auth = await requireAuth(req.headers.authorization);
    enforceQuota(auth.userId, 'chat');
    const input = z
      .object({
        question: z.string().min(1).max(400),
        context: z.object({
          sleep: z
            .object({
              recentScores: z.array(z.number().min(0).max(100)).max(30).default([]),
              recentDurationsMinutes: z.array(z.number().min(0).max(24 * 60)).max(30).default([]),
              recentDisturbances: z.array(z.number().min(0).max(100)).max(30).default([]),
            })
            .optional(),
          habits: z
            .object({
              completionRate7d: z.number().min(0).max(1).optional(),
              mostMissed: z.string().max(80).optional(),
            })
            .optional(),
          steps: z
            .object({
              recentSteps: z.array(z.number().min(0).max(200_000)).max(30).default([]),
            })
            .optional(),
          goals: z.array(z.string().min(1)).max(10).default([]),
        }),
      })
      .parse(req.body);

    const key = `chat:${cacheKey(input)}`;
    const cached = getCached<unknown>(key);
    if (cached) return z.object({ answer: z.string().min(1).max(1200) }).parse(cached);

  const client = getOpenAIClient();
  const system = [
    'You are SleepSense, a supportive habit and sleep coach.',
    'Use ONLY the provided context. If context is missing, ask one clarifying question.',
    'No medical diagnosis. No urgent claims. Be concise and actionable.',
  ].join(' ');

  const model = process.env.OPENAI_MODEL ?? 'gpt-4.1-mini';
    const resp = await client.chat.completions.create({
      model,
      temperature: 0.4,
      response_format: { type: 'json_object' },
      messages: [
        { role: 'system', content: system },
        {
          role: 'user',
          content: JSON.stringify({
            question: input.question,
            context: input.context,
            output: { answer: 'string (<=1200 chars)' },
          }),
        },
      ],
    });

    const raw = resp.choices[0]?.message?.content ?? '{}';
    const parsed = z.object({ answer: z.string().min(1).max(1200) }).parse(JSON.parse(raw));
    const out = {
      answer:
        parsed.answer.slice(0, 1200) ||
        'Could you share a bit more context from your last few nights?',
    };
    setCached(key, out);
    return out;
  });

  return app;
}

const app = buildServer();
const port = Number(process.env.PORT ?? 8000);
// Bind to 0.0.0.0 so devices on the same LAN (phone) can reach the dev backend.
// Override with HOST=127.0.0.1 if you really want loopback only.
const host = process.env.HOST ?? '0.0.0.0';

if (process.env.NODE_ENV !== 'test') {
  await app.listen({ port, host });
}

