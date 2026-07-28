package com.example.marketlens.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.marketlens.R

/**
 * Downloadable Google Fonts — no bundled assets.
 * Manrope drives display + body. JetBrains Mono handles tickers, prices, and
 * any tabular numerics.
 */
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

private fun manrope(weight: FontWeight) = Font(
    googleFont       = GoogleFont("Manrope"),
    fontProvider     = googleFontProvider,
    weight           = weight,
    style            = FontStyle.Normal
)

private fun mono(weight: FontWeight) = Font(
    googleFont       = GoogleFont("JetBrains Mono"),
    fontProvider     = googleFontProvider,
    weight           = weight,
    style            = FontStyle.Normal
)

val ManropeFamily = FontFamily(
    manrope(FontWeight.Light),
    manrope(FontWeight.Normal),
    manrope(FontWeight.Medium),
    manrope(FontWeight.SemiBold),
    manrope(FontWeight.Bold),
    manrope(FontWeight.ExtraBold),
)

val MonoFamily = FontFamily(
    mono(FontWeight.Normal),
    mono(FontWeight.Medium),
    mono(FontWeight.Bold),
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.ExtraBold,
        fontSize       = 48.sp,
        lineHeight     = 52.sp,
        letterSpacing  = (-1.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.ExtraBold,
        fontSize       = 36.sp,
        lineHeight     = 40.sp,
        letterSpacing  = (-1.0).sp
    ),
    displaySmall = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.Bold,
        fontSize       = 28.sp,
        lineHeight     = 32.sp,
        letterSpacing  = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.Bold,
        fontSize       = 26.sp,
        lineHeight     = 32.sp,
        letterSpacing  = (-0.4).sp
    ),
    headlineMedium = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.SemiBold,
        fontSize       = 22.sp,
        lineHeight     = 28.sp,
        letterSpacing  = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.SemiBold,
        fontSize       = 18.sp,
        lineHeight     = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.SemiBold,
        fontSize       = 18.sp,
        lineHeight     = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.SemiBold,
        fontSize       = 15.sp,
        lineHeight     = 20.sp,
        letterSpacing  = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.Medium,
        fontSize       = 13.sp,
        lineHeight     = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.Normal,
        fontSize       = 16.sp,
        lineHeight     = 24.sp,
        letterSpacing  = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.Normal,
        fontSize       = 14.sp,
        lineHeight     = 20.sp,
        letterSpacing  = 0.15.sp
    ),
    bodySmall = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.Normal,
        fontSize       = 12.sp,
        lineHeight     = 16.sp,
        letterSpacing  = 0.2.sp
    ),
    // Uppercase tracked micro-labels — the terminal "MARKET / GAINERS" chrome
    labelLarge = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.Bold,
        fontSize       = 12.sp,
        lineHeight     = 16.sp,
        letterSpacing  = 1.4.sp
    ),
    labelMedium = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.Bold,
        fontSize       = 10.sp,
        lineHeight     = 14.sp,
        letterSpacing  = 1.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily     = ManropeFamily,
        fontWeight     = FontWeight.Medium,
        fontSize       = 10.sp,
        lineHeight     = 14.sp,
        letterSpacing  = 0.8.sp
    ),
)

/** Tabular / monospace styles for prices, tickers, quantities. */
val PriceStyle = TextStyle(
    fontFamily    = MonoFamily,
    fontWeight    = FontWeight.Medium,
    fontSize      = 16.sp,
    lineHeight    = 20.sp,
    letterSpacing = 0.sp
)

val PriceStyleLarge = TextStyle(
    fontFamily    = MonoFamily,
    fontWeight    = FontWeight.Bold,
    fontSize      = 32.sp,
    lineHeight    = 36.sp,
    letterSpacing = (-0.5).sp
)

val TickerStyle = TextStyle(
    fontFamily    = MonoFamily,
    fontWeight    = FontWeight.Bold,
    fontSize      = 13.sp,
    lineHeight    = 18.sp,
    letterSpacing = 0.5.sp
)
