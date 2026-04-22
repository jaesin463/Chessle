package com.xinxe.chessle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ChessGreen,
    onPrimary = Color.White,
    secondary = BoardLight,
    tertiary = GoldAccent,
    background = DarkGrey,
    surface = SurfaceGrey,
    onSurface = Color.White,
    onSurfaceVariant = Color.Gray
)

private val LightColorScheme = lightColorScheme(
    primary = ChessGreen,
    onPrimary = Color.White,
    secondary = BoardLight,
    tertiary = GoldAccent,
    background = Color.White,
    surface = Color(0xFFF1F1F1),
    onSurface = Color.Black,
    onSurfaceVariant = Color.Gray
)

@Composable
fun ChesssleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}