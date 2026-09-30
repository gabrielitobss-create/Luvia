# Luvia

**Luvia** é um app Android em **React Native + TypeScript** para assistência em Luau e Roblox Studio. O app nunca chama OpenRouter diretamente: ele conversa apenas com o backend da Luvia.

```text
React Native Android → Backend da Luvia → OpenRouter → modelo → Backend → app
```

## App React Native

- Tema escuro, layout responsivo, transições sutis, tela inicial com sugestões e menu lateral.
- Chat com mensagens à direita/esquerda, rolagem automática, histórico local da conversa, geração/cancelamento e erros legíveis.
- Blocos Roblox mostram tipo (`Script`, `LocalScript`, `ModuleScript`), localização, explicação e botão **Copiar**.
- `src/services/AiService.ts` mantém a UI independente do provedor: `BackendAiService` chama somente `POST /api/chat`.

### URL do backend

O build de debug aponta para `http://10.0.2.2:8080/` (host da máquina no emulador Android). Para dispositivo físico ou produção, compile com HTTPS:

```bash
./gradlew -p android assembleDebug -PLUVIA_BACKEND_URL=https://seu-backend.exemplo/
```

A URL não é segredo. Nenhuma chave OpenRouter é empacotada no APK.

## Backend e OpenRouter

O backend Node.js fica em `backend/`, recebe uma mensagem em `/api/chat`, valida o conteúdo e envia a requisição ao OpenRouter com um prompt próprio da Luvia. Ele responde em pt-BR, prioriza cliente/servidor, `RemoteEvent`, `RemoteFunction`, `ReplicatedStorage`, `ServerScriptService`, `StarterPlayer`, `StarterGui`, segurança, replicação e performance. Solicitações para invadir terceiros, roubar credenciais ou dados continuam recusadas; segurança e anti-exploit para experiências próprias são permitidos.

1. Crie seu arquivo local, que é ignorado pelo Git:

   ```bash
   cp .env.example backend/.env
   ```

2. Preencha `backend/.env` **somente na sua máquina/host** e exporte-o:

   ```bash
   set -a; . backend/.env; set +a
   export OPENROUTER_MODEL="openrouter/free"
   ```

3. Inicie e teste:

   ```bash
   cd backend
   npm test
   npm start
   curl http://localhost:8080/health
   ```

`OPENROUTER_API_KEY` é obrigatório e é lido exclusivamente do ambiente do backend. `OPENROUTER_MODEL` é opcional; o padrão é `openrouter/free`.

### Hospedagem segura

No painel do serviço que hospeda **o backend** (Render, Railway, Fly.io ou equivalente), abra **Environment Variables**/**Secrets** e crie:

- `OPENROUTER_API_KEY`: sua chave real, marcada como segredo;
- `OPENROUTER_MODEL`: `openrouter/free` inicialmente ou outro modelo disponível na conta.

Nunca cadastre a chave no GitHub Actions, no APK, em arquivos Gradle ou no repositório.

## Desenvolvimento e APK

Requisitos: Node.js 20+, JDK 17, Android SDK Platform 35, `curl`/`wget` e `unzip`.

```bash
npm install
npm run typecheck
npm start
# em outro terminal, com emulador ou dispositivo conectado:
npm run android
# ou apenas gerar o APK:
./gradlew -p android assembleDebug
```

O APK é gerado em `android/app/build/outputs/apk/debug/app-debug.apk`.

## CI

O workflow **Build debug APK** instala dependências React Native, verifica TypeScript, testa o backend, compila o APK e publica `luvia-debug-apk`. Nenhum segredo é necessário no CI.
