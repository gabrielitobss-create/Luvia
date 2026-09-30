import {backendUrl} from '../config/backend';

export type CodeBlock = {scriptType: string; placement: string; code: string; explanation?: string};
export type AiAnswer = {reply: string; codeBlocks: CodeBlock[]};
export interface AiService { answer(message: string, signal?: AbortSignal): Promise<AiAnswer>; }

export class BackendAiService implements AiService {
  async answer(message: string, signal?: AbortSignal): Promise<AiAnswer> {
    let response: Response;
    try {
      response = await fetch(`${backendUrl}/api/chat`, {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({message}), signal});
    } catch (error) {
      if ((error as Error).name === 'AbortError') throw new Error('Geração interrompida.');
      throw new Error('Não foi possível conectar ao backend da Luvia.');
    }
    const body = await response.json().catch(() => null);
    if (!response.ok) throw new Error(body?.error || 'O backend não conseguiu responder.');
    if (typeof body?.reply !== 'string') throw new Error('O backend retornou uma resposta inválida.');
    return {reply: body.reply, codeBlocks: Array.isArray(body.codeBlocks) ? body.codeBlocks : []};
  }
}
