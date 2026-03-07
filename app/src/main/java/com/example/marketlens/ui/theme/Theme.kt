package com.example.marketlens.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary          = PrimaryDark,
    onPrimary        = OnPrimary,
    background       = BackgroundDark,
    onBackground     = OnSurfaceDark,
    surface          = SurfaceDark,
    onSurface        = OnSurfaceDark,
    surfaceVariant   = SurfaceVariantDark,
    onSurfaceVariant = MutedDark
)

private val LightColors = lightColorScheme(
    primary          = PrimaryLight,
    onPrimary        = OnPrimary,
    background       = BackgroundLight,
    onBackground     = OnSurfaceLight,
    surface          = SurfaceLight,
    onSurface        = OnSurfaceLight,
    surfaceVariant   = SurfaceVariantLight,
    onSurfaceVariant = MutedLight
)

@Composable
fun MarketLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}