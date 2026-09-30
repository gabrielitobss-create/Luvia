package com.luvia.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LuviaColors = darkColorScheme(
    primary = Color(0xFF9AA8FF), secondary = Color(0xFF8BE0C6),
    background = Color(0xFF0B1020), surface = Color(0xFF131A2E),
    surfaceVariant = Color(0xFF202944), onBackground = Color(0xFFE9ECF5),
    onSurface = Color(0xFFE9ECF5), onSurfaceVariant = Color(0xFFBFC7E4),
)

@Composable
fun LuviaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LuviaColors, typography = androidx.compose.material3.Typography(), content = content)
}
