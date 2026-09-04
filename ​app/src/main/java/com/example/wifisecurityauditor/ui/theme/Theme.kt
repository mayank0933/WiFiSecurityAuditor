package com.example.wifisecurityauditor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81D4FA),
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFFFB74D),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color(0xFF00363A),
    onSurface = Color(0xFFECEFF1)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0288D1),
    secondary = Color(0xFF00897B),
    tertiary = Color(0xFFFB8C00),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSurface = Color(0xFF212121)
)

@Composable
fun WiFiSecurityAuditorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

