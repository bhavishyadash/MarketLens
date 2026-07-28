package com.example.marketlens.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark-only Bloomberg-terminal theme. Amber primary, cyan secondary,
 * near-black canvas with sharp shapes and downloadable typography.
 */
private val TerminalColors = darkColorScheme(
    primary            = Amber,
    onPrimary          = TextInverse,
    primaryContainer   = AmberDim,
    onPrimaryContainer = Amber,

    secondary            = Cyan,
    onSecondary          = TextInverse,
    secondaryContainer   = TerminalRaised,
    onSecondaryContainer = Cyan,

    tertiary            = PriceUp,
    onTertiary          = TextInverse,
    tertiaryContainer   = TerminalRaised,
    onTertiaryContainer = PriceUp,

    background        = TerminalBlack,
    onBackground      = TextPrimary,

    surface           = TerminalSurface,
    onSurface         = TextPrimary,
    surfaceVariant    = TerminalRaised,
    onSurfaceVariant  = TextSecondary,
    surfaceTint       = Amber,

    outline           = TerminalBorder,
    outlineVariant    = TerminalBorder,

    error             = ErrorRed,
    onError           = TextInverse,
    errorContainer    = TerminalRaised,
    onErrorContainer  = ErrorRed,

    scrim             = TerminalBlack,
    inverseOnSurface  = TextInverse,
    inverseSurface    = TextPrimary,
    inversePrimary    = AmberDim
)

@Composable
fun MarketLensTheme(
    // Kept for API compatibility, but the theme is dark-only by design.
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor    = TerminalBlack.toArgb()
            window.navigationBarColor = TerminalBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = TerminalColors,
        typography  = Typography,
        shapes      = AppShapes,
        content     = content
    )
}
