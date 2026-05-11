import crypto from 'node:crypto';
import { z } from 'zod';

const SpotifyTokenResponse = z.object({
  access_token: z.string().min(1),
  token_type: z.string().min(1),
  scope: z.string().optional(),
  expires_in: z.number().int().min(1),
  refresh_token: z.string().optional(),
});

export type SpotifyTokenResponse = z.infer<typeof SpotifyTokenResponse>;

function base64UrlEncode(buf: Buffer) {
  return buf
    .toString('base64')
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/g, '');
}

export function pkceChallenge(verifier: string) {
  const hash = crypto.createHash('sha256').update(verifier).digest();
  return base64UrlEncode(hash);
}

export function createVerifier() {
  return base64UrlEncode(crypto.randomBytes(48));
}

export function buildAuthorizeUrl(params: {
  clientId: string;
  redirectUri: string;
  state: string;
  codeChallenge: string;
  scopes: string[];
}) {
  const url = new URL('https://accounts.spotify.com/authorize');
  url.searchParams.set('response_type', 'code');
  url.searchParams.set('client_id', params.clientId);
  url.searchParams.set('redirect_uri', params.redirectUri);
  url.searchParams.set('state', params.state);
  url.searchParams.set('code_challenge_method', 'S256');
  url.searchParams.set('code_challenge', params.codeChallenge);
  url.searchParams.set('scope', params.scopes.join(' '));
  return url.toString();
}

export async function exchangeCodeForToken(args: {
  clientId: string;
  redirectUri: string;
  code: string;
  codeVerifier: string;
}): Promise<SpotifyTokenResponse> {
  const body = new URLSearchParams();
  body.set('grant_type', 'authorization_code');
  body.set('client_id', args.clientId);
  body.set('code', args.code);
  body.set('redirect_uri', args.redirectUri);
  body.set('code_verifier', args.codeVerifier);

  const resp = await fetch('https://accounts.spotify.com/api/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: body.toString(),
  });
  const raw = await resp.text();
  if (!resp.ok) {
    throw new Error(`Spotify token exchange failed: ${resp.status} ${raw}`);
  }
  return SpotifyTokenResponse.parse(JSON.parse(raw));
}

export async function refreshSpotifyToken(args: {
  clientId: string;
  refreshToken: string;
}): Promise<SpotifyTokenResponse> {
  const body = new URLSearchParams();
  body.set('grant_type', 'refresh_token');
  body.set('client_id', args.clientId);
  body.set('refresh_token', args.refreshToken);

  const resp = await fetch('https://accounts.spotify.com/api/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: body.toString(),
  });
  const raw = await resp.text();
  if (!resp.ok) {
    throw new Error(`Spotify token refresh failed: ${resp.status} ${raw}`);
  }
  return SpotifyTokenResponse.parse(JSON.parse(raw));
}

export async function spotifyApiGet<T>(accessToken: string, url: string, schema: z.ZodSchema<T>) {
  const resp = await fetch(url, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  const raw = await resp.text();
  if (!resp.ok) throw new Error(`Spotify API failed: ${resp.status} ${raw}`);
  return schema.parse(JSON.parse(raw));
}
