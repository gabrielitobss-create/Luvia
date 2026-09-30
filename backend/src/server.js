import http from 'node:http';
import { createOpenRouterClient, UpstreamError } from './openrouter-client.js';

const MAX_BODY_BYTES = 16 * 1024;
const MAX_MESSAGE_LENGTH = 4_000;

function sendJson(response, status, body) {
  response.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8', 'Access-Control-Allow-Origin': process.env.CORS_ORIGIN || '*' });
  response.end(JSON.stringify(body));
}

async function readJson(request) {
  let raw = '';
  for await (const chunk of request) {
    raw += chunk;
    if (Buffer.byteLength(raw) > MAX_BODY_BYTES) throw new UpstreamError('Mensagem muito grande.', 413);
  }
  try { return JSON.parse(raw); } catch { throw new UpstreamError('JSON inválido.', 400); }
}

export function createLuviaServer(client) {
  return http.createServer(async (request, response) => {
    if (request.method === 'OPTIONS') return sendJson(response, 204, {});
    if (request.method === 'GET' && request.url === '/health') return sendJson(response, 200, { status: 'ok' });
    if (request.method !== 'POST' || request.url !== '/api/chat') return sendJson(response, 404, { error: 'Rota não encontrada.' });
    try {
      const { message } = await readJson(request);
      if (typeof message !== 'string' || !message.trim()) throw new UpstreamError('A mensagem é obrigatória.', 400);
      if (message.length > MAX_MESSAGE_LENGTH) throw new UpstreamError('A mensagem excede o limite de 4000 caracteres.', 400);
      sendJson(response, 200, await client.answer(message.trim()));
    } catch (error) {
      const status = error instanceof UpstreamError ? error.status : 500;
      const message = error instanceof Error ? error.message : 'Erro interno do backend.';
      sendJson(response, status, { error: message });
    }
  });
}

if (import.meta.url === `file://${process.argv[1]}`) {
  const client = createOpenRouterClient({ apiKey: process.env.OPENROUTER_API_KEY });
  const port = Number(process.env.PORT || 8080);
  createLuviaServer(client).listen(port, () => console.log(`Luvia backend ouvindo em http://localhost:${port}`));
}
