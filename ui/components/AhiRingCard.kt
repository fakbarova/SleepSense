package com.circadianx.sleepsense.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.circadianx.sleepsense.data.model.RiskLevel
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme

/**
 * Circular AHI score ring — the hero element on the Dashboard.
 * Colors: Green (low), Yellow (medium), Red (high), Purple (recording).
 */
@Composable
fun AhiRingCard(
    ahi: Float?,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    isRecording: Boolean = false
) {
    val colors = SleepSenseTheme.colors
    val risk = ahi?.let { RiskLevel.fromAhi(it) }

    val ringColor = when {
        isRecording -> colors.purple
        risk == RiskLevel.LOW    -> colors.green
        risk == RiskLevel.MEDIUM -> colors.yellow
        risk == RiskLevel.HIGH   -> colors.red
        else -> colors.textMuted
    }

    // Glow pulse for recording state
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue  = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            // Glow ring (blurred with alpha)
            Canvas(modifier = Modifier.size(size + 20.dp)) {
                drawCircle(
                    color  = ringColor.copy(alpha = if (isRecording) glowAlpha else 0.20f),
                    radius = (size.toPx() / 2f) + 8.dp.toPx(),
                    style  = Stroke(width = 16.dp.toPx())
                )
            }
            // Main ring
            Canvas(modifier = Modifier.size(size)) {
                drawCircle(
                    color  = ringColor,
                    radius = size.toPx() / 2f - 6.dp.toPx(),
                    style  = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            // Center content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when {
                    isRecording -> {
                        Text(
                            text     = "😴",
                            fontSize = 40.sp
                        )
                    }
                    ahi != null -> {
                        Text(
                            text       = "%.1f".format(ahi),
                            fontFamily = DmSerifDisplay,
                            fontWeight = FontWeight.Normal,
                            fontSize   = 42.sp,
                            color      = ringColor
                        )
                        Text(
                            text       = risk?.emoji ?: "",
                            fontSize   = 22.sp
                        )
                    }
                    else -> {
                        Text(text = "—", fontFamily = DmSerifDisplay, fontSize = 36.sp, color = colors.textMuted)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Label below ring
        Text(
            text       = when {
                isRecording  -> "Recording…"
                risk != null -> risk.label
                else         -> "No data"
            },
            fontFamily = JetBrainsMono,
            fontSize   = 11.sp,
            color      = colors.textMuted
        )
    }
}

/**
 * Small inline risk ring used in History list items.
 */
@Composable
fun MiniRiskRing(ahi: Float, size: Dp = 52.dp) {
    val colors = SleepSenseTheme.colors
    val risk = RiskLevel.fromAhi(ahi)
    val ringColor = when (risk) {
        RiskLevel.LOW    -> colors.green
        RiskLevel.MEDIUM -> colors.yellow
        RiskLevel.HIGH   -> colors.red
    }

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            drawCircle(
                color  = ringColor,
                radius = size.toPx() / 2f - 3.dp.toPx(),
                style  = Stroke(width = 2.5.dp.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "%.1f".format(ahi),
                fontFamily = DmSerifDisplay,
                fontSize   = 14.sp,
                color      = ringColor
            )
            Text(text = risk.emoji, fontSize = 10.sp)
        }
    }
}
