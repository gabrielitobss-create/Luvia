import { LUVIA_SYSTEM_PROMPT } from './luvia-prompt.js';

const OPENROUTER_URL = 'https://openrouter.ai/api/v1/chat/completions';
const DEFAULT_MODEL = 'openrouter/free';

export class UpstreamError extends Error {
  constructor(message, status = 502) {
    super(message);
    this.status = status;
  }
}

export function normalizeModelReply(content) {
  if (typeof content !== 'string' || !content.trim()) {
    throw new UpstreamError('O provedor retornou uma resposta vazia.');
  }
  const json = content.trim().replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/, '');
  try {
    const parsed = JSON.parse(json);
    const reply = typeof parsed.reply === 'string' ? parsed.reply.trim() : '';
    if (!reply) throw new Error('reply ausente');
    const codeBlocks = Array.isArray(parsed.codeBlocks)
      ? parsed.codeBlocks.flatMap((block) => {
          if (!block || typeof block.code !== 'string' || !block.code.trim()) return [];
          return [{
            scriptType: typeof block.scriptType === 'string' ? block.scriptType : 'Código',
            placement: typeof block.placement === 'string' ? block.placement : 'Não especificado',
            code: block.code.trim(),
            explanation: typeof block.explanation === 'string' ? block.explanation : '',
          }];
        })
      : [];
    return { reply, codeBlocks };
  } catch {
    // A resposta ainda é útil quando o modelo não obedece ao formato estruturado.
    return { reply: content.trim(), codeBlocks: [] };
  }
}

export function createOpenRouterClient({ apiKey, model = process.env.OPENROUTER_MODEL || DEFAULT_MODEL, fetchImpl = fetch }) {
  if (!apiKey) throw new Error('OPENROUTER_API_KEY não está configurada no backend.');

  return {
    async answer(message) {
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), 30_000);
      try {
        const response = await fetchImpl(OPENROUTER_URL, {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${apiKey}`,
            'Content-Type': 'application/json',
            'HTTP-Referer': 'https://luvia.local',
            'X-Title': 'Luvia Backend',
          },
          body: JSON.stringify({ model, messages: [{ role: 'system', content: LUVIA_SYSTEM_PROMPT }, { role: 'user', content: message }] }),
          signal: controller.signal,
        });
        const payload = await response.json().catch(() => null);
        if (!response.ok) {
          if (response.status === 429) throw new UpstreamError('O limite do provedor foi atingido. Tente novamente em instantes.', 429);
          throw new UpstreamError('Não foi possível obter uma resposta do provedor de IA.', 502);
        }
        return normalizeModelReply(payload?.choices?.[0]?.message?.content);
      } catch (error) {
        if (error instanceof UpstreamError) throw error;
        if (error?.name === 'AbortError') throw new UpstreamError('A resposta do provedor demorou demais. Tente novamente.', 504);
        throw new UpstreamError('Falha de conexão com o provedor de IA. Tente novamente.', 503);
      } finally {
        clearTimeout(timeout);
      }
    },
  };
}

export { DEFAULT_MODEL };
