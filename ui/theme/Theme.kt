package com.circadianx.sleepsense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Extended color palette exposed via SleepSenseTheme.colors ────────────────
@Immutable
data class SleepSenseColors(
    val bgDeep: Color,
    val bgBase: Color,
    val bgCard: Color,
    val bgElevated: Color,
    val purple: Color,
    val purpleDim: Color,
    val blue: Color,
    val green: Color,
    val yellow: Color,
    val red: Color,
    val sleepLight: Color,
    val sleepRem: Color,
    val sleepDeep: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val stroke: Color
)

val LocalSleepSenseColors = staticCompositionLocalOf {
    SleepSenseColors(
        bgDeep        = BgDeep,
        bgBase        = BgBase,
        bgCard        = BgCard,
        bgElevated    = BgElevated,
        purple        = Purple,
        purpleDim     = PurpleDim,
        blue          = Blue,
        green         = Green,
        yellow        = Yellow,
        red           = Red,
        sleepLight    = SleepLight,
        sleepRem      = SleepRem,
        sleepDeep     = SleepDeep,
        textPrimary   = TextPrimary,
        textSecondary = TextSecondary,
        textMuted     = TextMuted,
        border        = Border,
        stroke        = Stroke
    )
}

private val DarkColorScheme = darkColorScheme(
    primary        = Purple,
    secondary      = Blue,
    tertiary       = Green,
    background     = BgDeep,
    surface        = BgBase,
    surfaceVariant = BgCard,
    onPrimary      = Color.White,
    onBackground   = TextPrimary,
    onSurface      = TextPrimary,
    error          = Red,
    outline        = Border
)

@Composable
fun SleepSenseTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSleepSenseColors provides LocalSleepSenseColors.current) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography  = SleepSenseTypography,
            content     = content
        )
    }
}

// Convenience accessor
object SleepSenseTheme {
    val colors: SleepSenseColors
        @Composable get() = LocalSleepSenseColors.current
}
