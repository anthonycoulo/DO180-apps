package com.do180.dettegame.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1F3A93),
    onPrimary = Color.White,
    secondary = Color(0xFFC8102E),
    onSecondary = Color.White,
    background = Color(0xFFF6F7FB),
    surface = Color.White
)

@Composable
fun DetteGameTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColorScheme, content = content)
}
