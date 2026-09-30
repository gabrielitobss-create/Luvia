# Luvia

Luvia é um aplicativo Android nativo em Kotlin/Jetpack Compose que conversa com um **backend próprio**. O backend chama o OpenRouter; a chave do provedor nunca é enviada para o APK nem é versionada.

## Arquitetura

```text
Luvia Android → Backend da Luvia → OpenRouter → modelo → Backend → Luvia Android
```

- `app/`: app Android, interface Compose e `BackendAiService`. O app só envia `POST /api/chat` para o backend.
- `backend/`: servidor Node.js sem dependências externas. Ele mantém o prompt especializado, chama OpenRouter e valida/normaliza a resposta.
- `AiService`: contrato separado da UI. Trocar o provedor futuramente requer outra implementação do backend, sem alterar a tela de chat.

O prompt da Luvia responde em português do Brasil e exige que códigos Roblox informem tipo (`Script`, `LocalScript` ou `ModuleScript`), local de inserção e explicação. Também prioriza validação no servidor, `RemoteEvent`, `RemoteFunction`, `ReplicatedStorage`, `ServerScriptService`, `StarterPlayer`, `StarterGui`, módulos e replicação. Pedidos para invadir experiências de terceiros, roubar dados, credenciais ou burlar sistemas são recusados.

## Configurar o backend

Requisitos: Node.js 20+ e uma conta OpenRouter.

1. Copie o modelo de ambiente e preencha **apenas localmente**:

   ```bash
   cp .env.example backend/.env
   ```

2. Exporte as variáveis antes de iniciar. Em macOS/Linux:

   ```bash
   set -a
   . backend/.env
   set +a
   export OPENROUTER_MODEL="openrouter/free" # ou outro modelo disponível na sua conta
   ```

   Defina `OPENROUTER_API_KEY` no arquivo `backend/.env` (ou diretamente no ambiente). Nunca coloque essa chave no APK, no Gradle, no código ou no GitHub. `OPENROUTER_MODEL` é opcional: sem ela, o backend usa `openrouter/free`.

3. Execute e teste:

   ```bash
   cd backend
   npm test
   npm start
   curl http://localhost:8080/health
   curl -X POST http://localhost:8080/api/chat \
     -H 'Content-Type: application/json' \
     -d '{"message":"Como valido um RemoteEvent de compra?"}'
   ```

O servidor escuta em `http://localhost:8080` por padrão. Ele trata mensagens inválidas, indisponibilidade/conexão, timeout, limite do provedor (HTTP 429) e respostas inválidas sem expor a chave.

### Hospedagem e segredos

No painel do serviço que hospeda **o backend** (por exemplo, seção *Environment Variables*/*Secrets* do Render, Railway, Fly.io ou similar), cadastre:

- `OPENROUTER_API_KEY`: sua chave real do OpenRouter, marcada como secreta;
- `OPENROUTER_MODEL`: `openrouter/free` inicialmente, ou um identificador de modelo que sua conta possa usar.

Não cadastre a chave no GitHub Actions do APK nem no Android. Configure também o `PORT` somente se sua plataforma exigir; plataformas geralmente o fornecem automaticamente.

## Conectar o Android ao backend

O debug APK usa `http://10.0.2.2:8080/`, que aponta para a máquina local quando executado no emulador Android. Para um dispositivo físico ou backend hospedado, use HTTPS e passe a URL durante o build:

```bash
./gradlew assembleDebug -PLUVIA_BACKEND_URL=https://seu-backend.exemplo/
```

Depois abra o app, envie uma pergunta sobre Luau/Roblox e a resposta do endpoint `/api/chat` aparecerá na conversa. Para desenvolvimento local em dispositivo físico, substitua `10.0.2.2` pelo IP LAN da máquina e use uma rede confiável.

## Compilar o APK

Instale Android SDK Platform 35, JDK 17, `curl` (ou `wget`) e `unzip`, então execute:

```bash
./gradlew assembleDebug
```

O APK de debug é gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## CI

O workflow **Build debug APK** testa o backend, compila o APK com Java 17 e publica `app-debug.apk` como o artifact `luvia-debug-apk`. Nenhuma chave é necessária no CI para esses testes ou para a compilação.
