package com.notesync.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SlatePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDEEF4),
    secondary = SageAccent,
    onSecondary = Color.White,
    background = WarmBackground,
    surface = Color.White,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = DeleteRed,
    onBackground = TextPrimary,
)

@Composable
fun NoteSyncTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
