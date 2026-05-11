package com.circadianx.sleepsense.ui.theme

import androidx.compose.ui.graphics.Color

// ── Backgrounds ──────────────────────────────────────────────────────────────
val BgDeep      = Color(0xFF0B0F1A)
val BgBase      = Color(0xFF12182A)
val BgCard      = Color(0xFF1A2035)
val BgElevated  = Color(0xFF212840)   // cards-on-cards, sheet content

// ── Accent & Interactive ─────────────────────────────────────────────────────
val Purple      = Color(0xFFA970FF)
val Blue        = Color(0xFF5B9CF6)
val PurpleDim   = Color(0xFF7C4FCC)   // pressed / disabled variant

// ── Gradients (as individual stops — use Brush in components) ────────────────
val GradientStart = Color(0xFFA970FF)  // Purple
val GradientEnd   = Color(0xFF5B9CF6)  // Blue

// ── Health Risk ───────────────────────────────────────────────────────────────
val Green    = Color(0xFF34D399)   // Low risk
val Yellow   = Color(0xFFFBBF24)   // Med risk
val Red      = Color(0xFFF87171)   // High risk

// ── Sleep Stages ─────────────────────────────────────────────────────────────
val SleepLight      = Blue
val SleepRem        = Purple
val SleepDeep       = Color(0xFF3B1F6E)

// ── Text ─────────────────────────────────────────────────────────────────────
val TextPrimary     = Color(0xFFFFFFFF)
val TextSecondary   = Color(0xFFB8BFD0)   // bumped from A0A7B8 for WCAG AA
val TextMuted       = Color(0xFF8890A4)   // bumped from 6B7280 for AA on BgDeep

// ── Borders & Strokes ────────────────────────────────────────────────────────
val Border          = Color(0x1AFFFFFF)   // 10% white
val Stroke          = Color(0x26FFFFFF)   // 15% white — focused / selected
