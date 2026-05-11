import crypto from 'node:crypto';
import { z } from 'zod';

const AuthHeaderSchema = z
  .string()
  .regex(/^Bearer\s+.+$/i, 'Authorization header must be a Bearer token');

const TokenSchema = z.object({
  sub: z.string().min(1),
  iss: z.string().min(1),
  aud: z.union([z.string(), z.array(z.string())]).optional(),
  email: z.string().email().optional(),
  name: z.string().min(1).max(120).optional(),
});

const acceptedAudiences = (process.env.OAUTH_AUDIENCES ?? '')
  .split(',')
  .map((item) => item.trim())
  .filter(Boolean);

export type AuthUser = {
  userId: string;
  subject: string;
  issuer: string;
  email: string | null;
  name: string | null;
};

export class UnauthorizedError extends Error {
  constructor(message = 'Unauthorized') {
    super(message);
    this.name = 'UnauthorizedError';
  }
}

function toStableUserId(issuer: string, subject: string) {
  const raw = `${issuer}:${subject}`;
  return crypto.createHash('sha256').update(raw).digest('hex');
}

function audienceIsAccepted(aud: string | string[] | undefined) {
  if (acceptedAudiences.length === 0) return true;
  if (typeof aud === 'string') return acceptedAudiences.includes(aud);
  if (Array.isArray(aud)) return aud.some((entry) => acceptedAudiences.includes(entry));
  return false;
}

export function extractBearerToken(authHeader: string | undefined) {
  const parsed = AuthHeaderSchema.safeParse(authHeader ?? '');
  if (!parsed.success) {
    throw new UnauthorizedError(parsed.error.issues[0]?.message ?? 'Missing bearer token');
  }
  return parsed.data.replace(/^Bearer\s+/i, '').trim();
}

export async function verifyOAuthToken(idToken: string): Promise<AuthUser> {
  const response = await fetch(
    `https://oauth2.googleapis.com/tokeninfo?id_token=${encodeURIComponent(idToken)}`
  );
  if (!response.ok) {
    throw new UnauthorizedError('Invalid OAuth token');
  }
  const claims = TokenSchema.parse(await response.json());
  if (!audienceIsAccepted(claims.aud)) {
    throw new UnauthorizedError('Audience is not allowed');
  }
  return {
    userId: toStableUserId(claims.iss, claims.sub),
    subject: claims.sub,
    issuer: claims.iss,
    email: claims.email ?? null,
    name: claims.name ?? null,
  };
}
