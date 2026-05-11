import { test } from 'node:test';
import assert from 'node:assert/strict';

process.env.NODE_ENV = 'test';

const { buildServer } = await import('./server.js');

test('health endpoint works', async () => {
  const app = buildServer();
  const response = await app.inject({ method: 'GET', url: '/health' });
  assert.equal(response.statusCode, 200);
  assert.deepEqual(response.json(), { ok: true });
  await app.close();
});

test('protected endpoint rejects missing token', async () => {
  const app = buildServer();
  const response = await app.inject({ method: 'GET', url: '/users/me' });
  assert.equal(response.statusCode, 401);
  await app.close();
});
