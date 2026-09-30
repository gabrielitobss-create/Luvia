package com.luvia.ai.data

import com.luvia.ai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/** Provider boundary: the Compose UI only depends on this contract. */
interface AiService {
    suspend fun answer(message: String): AiAnswer
}

data class CodeBlock(
    val scriptType: String,
    val placement: String,
    val code: String,
    val explanation: String = "",
)

data class AiAnswer(
    val text: String,
    val codeBlocks: List<CodeBlock> = emptyList(),
)

class AiServiceException(message: String) : Exception(message)

/** Calls Luvia's backend only; OpenRouter credentials never enter the Android app. */
class BackendAiService(
    private val baseUrl: String = BuildConfig.BACKEND_BASE_URL,
) : AiService {
    override suspend fun answer(message: String): AiAnswer = withContext(Dispatchers.IO) {
        val connection = try {
            (URL(baseUrl.trimEnd('/') + "/api/chat").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 35_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
        } catch (_: Exception) {
            throw AiServiceException("Não foi possível conectar ao backend da Luvia.")
        }

        try {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(JSONObject().put("message", message).toString())
            }
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use(BufferedReader::readText)
                .orEmpty()
            val json = runCatching { JSONObject(body) }.getOrNull()
            if (status !in 200..299) {
                throw AiServiceException(json?.optString("error")?.ifBlank { null }
                    ?: "O backend não conseguiu responder. Tente novamente.")
            }
            val reply = json?.optString("reply")?.trim().orEmpty()
            if (reply.isBlank()) throw AiServiceException("O backend retornou uma resposta inválida.")
            AiAnswer(reply, json?.optJSONArray("codeBlocks").toCodeBlocks())
        } catch (error: AiServiceException) {
            throw error
        } catch (_: Exception) {
            throw AiServiceException("Falha ao processar a resposta da Luvia. Tente novamente.")
        } finally {
            connection.disconnect()
        }
    }
}

private fun JSONArray?.toCodeBlocks(): List<CodeBlock> = buildList {
    if (this@toCodeBlocks == null) return@buildList
    for (index in 0 until length()) {
        val item = optJSONObject(index) ?: continue
        val code = item.optString("code").trim()
        if (code.isNotEmpty()) add(CodeBlock(
            scriptType = item.optString("scriptType", "Código"),
            placement = item.optString("placement", "Não especificado"),
            code = code,
            explanation = item.optString("explanation"),
        ))
    }
}
