# Luvia

Aplicativo Android nativo, leve e em português do Brasil para assistência com **Luau** e **Roblox Studio**. A primeira versão traz uma interface de chat escura, renderização de exemplos de código e uma arquitetura preparada para conectar um provedor de IA futuramente.

## Recursos

- Chat moderno em Jetpack Compose, com tema escuro padrão.
- Mensagens do usuário e da Luvia, campo de envio e tela de configurações.
- Blocos de código monoespaçados com indicação de `Script`, `LocalScript` ou `ModuleScript`, local sugerido no Studio e botão para copiar.
- Fronteira de serviço `AiService`: o app usa uma resposta local de demonstração e não inclui chaves, tokens ou credenciais.
- Base de especialização para Luau/Roblox: cliente e servidor, `RemoteEvent`, `RemoteFunction`, `ReplicatedStorage`, `ServerScriptService`, replicação, segurança e performance.

## Arquitetura

- `ui/`: telas Compose e tema.
- `data/AiService.kt`: contrato independente de provedor e implementação local de desenvolvimento. Uma integração de rede deve implementar `AiService` fora da UI e obter credenciais de um backend seguro — nunca do APK.

A Luvia deve responder em pt-BR e, ao recomendar código, informar o tipo de script e onde inseri-lo no Roblox Studio. Integrações futuras devem validar APIs contra a documentação oficial do Roblox e manter validações sensíveis no servidor.

## Compilar

1. Instale o Android Studio com Android SDK Platform 35, JDK 17, `curl` (ou `wget`) e `unzip`.
2. Abra esta pasta no Android Studio ou, no terminal, execute:

   ```bash
   ./gradlew assembleDebug
   ```

3. O script `gradlew` baixa automaticamente a distribuição definida em `gradle/wrapper/gradle-wrapper.properties`; o APK será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## CI

O workflow em `.github/workflows/android-debug.yml` cria um APK de debug e o publica como artefato a cada push e pull request.
