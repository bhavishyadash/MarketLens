package com.example.marketlens.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ─── Dark Color Scheme ────────────────────────────────────────────────────────
// All Material 3 slots mapped to our finance palette.
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

// ─── Light Color Scheme ───────────────────────────────────────────────────────
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
    /*
        Bug fixed: dynamicColor was TRUE by default.
        Dynamic color (Android 12+) replaces ALL your custom colors
        with colors pulled from the user's wallpaper — your finance app
        would look completely random on every phone.
        We always use our own defined palette.
    */
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
