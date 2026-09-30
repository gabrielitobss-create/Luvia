package com.luvia.ai.data

/** Provider boundary: replace [LocalLuviaService] with an HTTP-backed implementation later. */
interface AiService {
    suspend fun answer(message: String): AiAnswer
}

data class AiAnswer(
    val text: String,
    val code: String? = null,
    val scriptType: String? = null,
    val placement: String? = null,
)

/** A safe offline placeholder that keeps the UI functional without credentials or network access. */
class LocalLuviaService : AiService {
    override suspend fun answer(message: String): AiAnswer = AiAnswer(
        text = "Posso ajudar com Luau e Roblox Studio. Para eventos entre cliente e servidor, valide sempre os dados no servidor.",
        code = "-- ServerScriptService/ComprarItem.server.lua\nlocal ReplicatedStorage = game:GetService(\"ReplicatedStorage\")\nlocal comprarItem = ReplicatedStorage:WaitForChild(\"ComprarItem\")\n\ncomprarItem.OnServerEvent:Connect(function(player, itemId)\n    if typeof(itemId) ~= \"string\" then return end\n    -- Valide preço, saldo e permissão aqui no servidor.\nend)",
        scriptType = "Script",
        placement = "ServerScriptService",
    )
}
