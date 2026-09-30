package com.luvia.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luvia.ai.data.AiAnswer
import com.luvia.ai.data.AiService
import com.luvia.ai.data.BackendAiService
import kotlinx.coroutines.launch

private data class ChatMessage(val fromLuvia: Boolean, val body: String, val answer: AiAnswer? = null)

@Composable
fun LuviaApp(service: AiService = BackendAiService()) {
    var settingsOpen by remember { mutableStateOf(false) }
    if (settingsOpen) SettingsScreen(onBack = { settingsOpen = false }) else ChatScreen(service, { settingsOpen = true })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreen(service: AiService, onSettings: () -> Unit) {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf(
        ChatMessage(true, "Olá! Sou a Luvia, sua assistente de Luau e Roblox Studio. O que vamos construir hoje?")
    ) }
    Scaffold(
        topBar = { TopAppBar(
            title = { Column { Text("Luvia", fontWeight = FontWeight.Bold); Text("Luau • Roblox Studio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            actions = { IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Configurações") } }
        ) },
        bottomBar = { Surface(color = MaterialTheme.colorScheme.background) {
            Row(Modifier.padding(12.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), placeholder = { Text("Pergunte sobre Luau...") }, maxLines = 4, shape = RoundedCornerShape(22.dp))
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = {
                    val prompt = input.trim(); if (prompt.isNotEmpty() && !loading) {
                        messages += ChatMessage(false, prompt); input = ""; loading = true
                        scope.launch {
                            val response = runCatching { service.answer(prompt) }
                            val answer = response.getOrElse { error ->
                                AiAnswer(error.message ?: "Não foi possível obter uma resposta agora.")
                            }
                            messages += ChatMessage(true, answer.text, answer)
                            loading = false
                        }
                    }
                }, enabled = input.isNotBlank() && !loading, modifier = Modifier.size(52.dp)) { Icon(Icons.Default.ArrowUpward, "Enviar") }
            }
        } }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
            items(messages) { MessageBubble(it) }
            if (loading) item { Text("Luvia está preparando uma resposta…", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val alignment = if (message.fromLuvia) Alignment.Start else Alignment.End
    val color = if (message.fromLuvia) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    Column(Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Text(if (message.fromLuvia) "LUVIA" else "VOCÊ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Column(Modifier.clip(RoundedCornerShape(18.dp)).background(color).padding(14.dp).widthIn(max = 340.dp)) {
            Text(message.body, color = if (message.fromLuvia) MaterialTheme.colorScheme.onSurface else Color(0xFF101426))
            message.answer?.codeBlocks?.forEach { block -> CodeBlock(block.code, block.scriptType, block.placement, block.explanation) }
        }
    }
}

@Composable
private fun CodeBlock(code: String, scriptType: String, placement: String, explanation: String) {
    val clipboard = LocalClipboardManager.current
    Spacer(Modifier.height(12.dp))
    Surface(color = Color(0xFF090E1B), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("$scriptType · $placement", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                IconButton(onClick = { clipboard.setText(AnnotatedString(code)) }) { Icon(Icons.Default.ContentCopy, "Copiar código", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Text(code, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = Color(0xFFD7E2FF))
            if (explanation.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Configurações") }, navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }) }) { padding ->
        Column(Modifier.padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Luvia", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("A conexão com um provedor de IA será configurada por uma implementação de AiService. Nenhuma chave é armazenada no aplicativo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            Text("Idioma", style = MaterialTheme.typography.titleMedium)
            Text("Português (Brasil)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Tema", style = MaterialTheme.typography.titleMedium)
            Text("Escuro", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
