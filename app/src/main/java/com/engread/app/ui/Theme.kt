package com.engread.app.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.engread.app.data.ReaderTheme

private val DuoGreen = Color(0xFF58CC02)
private val Ink = Color(0xFF1F2933)
private val Paper = Color(0xFFFFFBF1)
private val Mint = Color(0xFFE8F8DF)
private val Sky = Color(0xFFEAF5FF)
private val Coral = Color(0xFFFFE9DF)

@Composable
fun EngReadTheme(
    readerTheme: ReaderTheme,
    content: @Composable () -> Unit,
) {
    val scheme = when (readerTheme) {
        ReaderTheme.LIGHT -> lightEngReadScheme()
        ReaderTheme.PAPER -> paperScheme()
        ReaderTheme.DARK -> darkEngReadScheme()
    }
    MaterialTheme(
        colorScheme = scheme,
        content = content,
    )
}

private fun lightEngReadScheme(): ColorScheme =
    lightColorScheme(
        primary = DuoGreen,
        onPrimary = Color.White,
        primaryContainer = Mint,
        onPrimaryContainer = Ink,
        secondary = Color(0xFF1CB0F6),
        secondaryContainer = Sky,
        tertiary = Color(0xFFFF7A59),
        tertiaryContainer = Coral,
        background = Color(0xFFF8FAF5),
        onBackground = Ink,
        surface = Color.White,
        onSurface = Ink,
        surfaceVariant = Color(0xFFE9EFE4),
        outline = Color(0xFFD5DED0),
    )

private fun paperScheme(): ColorScheme =
    lightColorScheme(
        primary = DuoGreen,
        onPrimary = Color.White,
        primaryContainer = Mint,
        onPrimaryContainer = Ink,
        secondary = Color(0xFF2E7D80),
        secondaryContainer = Color(0xFFDDF1EC),
        tertiary = Color(0xFFBF6B45),
        tertiaryContainer = Color(0xFFFFE1D2),
        background = Paper,
        onBackground = Ink,
        surface = Color(0xFFFFFDF7),
        onSurface = Ink,
        surfaceVariant = Color(0xFFF3E9D4),
        outline = Color(0xFFE2D8C5),
    )

private fun darkEngReadScheme(): ColorScheme =
    darkColorScheme(
        primary = DuoGreen,
        onPrimary = Color(0xFF102000),
        primaryContainer = Color(0xFF326F05),
        onPrimaryContainer = Color(0xFFE6FFD6),
        secondary = Color(0xFF8AD7FF),
        secondaryContainer = Color(0xFF12455F),
        tertiary = Color(0xFFFFB49E),
        tertiaryContainer = Color(0xFF733B28),
        background = Color(0xFF101814),
        onBackground = Color(0xFFE7F0E8),
        surface = Color(0xFF17211B),
        onSurface = Color(0xFFE7F0E8),
        surfaceVariant = Color(0xFF28342D),
        outline = Color(0xFF5D6B60),
    )
