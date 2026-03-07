package com.example.marketlens.ui.theme

import androidx.compose.ui.graphics.Color

// ─── App Background ───────────────────────────────────────────────────────────
// The deepest background — what you see behind everything.
val BackgroundDark  = Color(0xFF0D0F14)
val BackgroundLight = Color(0xFFF2F4F8)

// ─── Surface (Cards, Sheets) ──────────────────────────────────────────────────
// Cards sit one level above the background.
val SurfaceDark  = Color(0xFF1A1D23)
val SurfaceLight = Color(0xFFFFFFFF)

// ─── Surface Variant (dividers, subtle containers) ───────────────────────────
val SurfaceVariantDark  = Color(0xFF2A2F3A)
val SurfaceVariantLight = Color(0xFFE0E4EF)

// ─── Primary (interactive elements, highlights) ───────────────────────────────
// Blue is universally associated with finance / trust.
val PrimaryDark  = Color(0xFF2196F3)
val PrimaryLight = Color(0xFF1565C0)

// ─── On-Primary (text/icons that sit ON the primary color) ───────────────────
val OnPrimary = Color(0xFFFFFFFF)

// ─── On-Surface (default text color) ─────────────────────────────────────────
val OnSurfaceDark  = Color(0xFFE8EAF0)
val OnSurfaceLight = Color(0xFF0D0F14)

// ─── Muted text (labels, timestamps, secondary info) ─────────────────────────
val MutedDark  = Color(0xFF7A8394)
val MutedLight = Color(0xFF5C6370)

// ─── Semantic: up / down (used for price changes everywhere) ──────────────────
val PriceUp   = Color(0xFF26A65B)  // green
val PriceDown = Color(0xFFE53935)  // red
