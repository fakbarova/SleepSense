package com.circadianx.sleepsense.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Font families ─────────────────────────────────────────────────────────────
// System fallbacks used by default so the project compiles and runs immediately.
// To use the exact design fonts (DM Serif Display, DM Sans, JetBrains Mono),
// add `ui-text-google-fonts` and replace these with GoogleFont families.

val DmSerifDisplay = FontFamily.Serif       // → DM Serif Display
val DmSans         = FontFamily.SansSerif   // → DM Sans
val JetBrainsMono  = FontFamily.Monospace   // → JetBrains Mono

val SleepSenseTypography = Typography(
    // Hero AHI number
    displayLarge = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize   = 48.sp,
        lineHeight = 52.sp
    ),
    // Section headings
    headlineMedium = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize   = 24.sp,
        lineHeight = 28.sp
    ),
    // Card titles
    titleMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize   = 13.sp,
        lineHeight = 18.sp
    ),
    // Body
    bodyLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 15.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp,
        lineHeight = 20.sp
    ),
    // Mono labels / tags
    labelSmall = TextStyle(
        fontFamily    = JetBrainsMono,
        fontWeight    = FontWeight.Normal,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 14.sp
    )
)
