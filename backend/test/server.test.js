import assert from 'node:assert/strict';
import test from 'node:test';
import { once } from 'node:events';
import { createLuviaServer } from '../src/server.js';

async function withServer(client, callback) {
  const server = createLuviaServer(client);
  server.listen(0, '127.0.0.1');
  await once(server, 'listening');
  try {
    await callback(`http://127.0.0.1:${server.address().port}`);
  } finally {
    server.close();
    await once(server, 'close');
  }
}

test('accepts a chat message and returns the backend response', async () => {
  await withServer({ answer: async (message) => ({ reply: `Recebi: ${message}`, codeBlocks: [] }) }, async (url) => {
    const response = await fetch(`${url}/api/chat`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ message: 'Olá' }),
    });
    assert.equal(response.status, 200);
    assert.deepEqual(await response.json(), { reply: 'Recebi: Olá', codeBlocks: [] });
  });
});

test('rejects empty chat messages before contacting OpenRouter', async () => {
  await withServer({ answer: async () => { throw new Error('não deveria ser chamado'); } }, async (url) => {
    const response = await fetch(`${url}/api/chat`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ message: ' ' }),
    });
    assert.equal(response.status, 400);
    assert.equal((await response.json()).error, 'A mensagem é obrigatória.');
  });
});
