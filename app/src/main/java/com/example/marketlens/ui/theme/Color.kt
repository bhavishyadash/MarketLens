package com.example.marketlens.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * MarketLens palette — Bloomberg terminal x modern.
 * Dark-only. Amber primary, cyan data accent, vivid gain/loss.
 */

// Neutrals — near-black terminal void, layered gray strata
val TerminalBlack   = Color(0xFF050505)   // app background
val TerminalSurface = Color(0xFF0D0D0F)   // card surface
val TerminalRaised  = Color(0xFF141418)   // inner / raised surface
val TerminalElevate = Color(0xFF1A1A1F)   // dropdowns, dialogs
val TerminalBorder  = Color(0xFF26262E)   // hairline dividers
val TerminalDim     = Color(0xFF1F1F25)   // subtle chip fill

// Text
val TextPrimary   = Color(0xFFF2F2F5)
val TextSecondary = Color(0xFFA8A8B2)
val TextTertiary  = Color(0xFF6B6B75)
val TextInverse   = Color(0xFF050505)

// Accents — Bloomberg amber + cyan data
val Amber       = Color(0xFFFFB000)
val AmberDim    = Color(0xFF7A5300)
val AmberSoft   = Color(0x33FFB000)
val Cyan        = Color(0xFF00E0FF)
val CyanDim     = Color(0x3300E0FF)

// Semantic — market gains / losses
val PriceUp   = Color(0xFF00E676)
val PriceDown = Color(0xFFFF3D3D)

// Status
val LiveDot    = Color(0xFF00E676)
val WarningAmb = Color(0xFFFFB000)
val ErrorRed   = Color(0xFFFF3D3D)
