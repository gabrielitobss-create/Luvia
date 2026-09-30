import assert from 'node:assert/strict';
import test from 'node:test';
import { createOpenRouterClient, normalizeModelReply, UpstreamError } from '../src/openrouter-client.js';

test('normalizes a structured Luvia reply with a Roblox code block', () => {
  const answer = normalizeModelReply('{"reply":"Use validação.","codeBlocks":[{"scriptType":"Script","placement":"ServerScriptService","code":"print(1)","explanation":"Executa no servidor."}]}');
  assert.equal(answer.reply, 'Use validação.');
  assert.deepEqual(answer.codeBlocks[0], { scriptType: 'Script', placement: 'ServerScriptService', code: 'print(1)', explanation: 'Executa no servidor.' });
});

test('keeps a non-JSON provider response as plain text', () => {
  assert.deepEqual(normalizeModelReply('Resposta sem JSON'), { reply: 'Resposta sem JSON', codeBlocks: [] });
});

test('maps OpenRouter rate limits to a friendly error', async () => {
  const client = createOpenRouterClient({
    apiKey: 'injected-only-for-test',
    fetchImpl: async () => new Response(JSON.stringify({ error: {} }), { status: 429 }),
  });
  await assert.rejects(() => client.answer('oi'), (error) => error instanceof UpstreamError && error.status === 429);
});
