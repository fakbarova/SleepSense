package com.circadianx.sleepsense.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.GradientEnd
import com.circadianx.sleepsense.ui.theme.GradientStart
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import kotlin.math.roundToInt

@Composable
fun SsScoreRing(
    score: Int?,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 12.dp,
    label: String = "Sleep score"
) {
    val colors = SleepSenseTheme.colors
    val fraction = remember { Animatable(0f) }
    val displayScore = if (score == null) null else (fraction.value * 100).roundToInt().coerceAtMost(score)
    val (qualityLabel, qualityColor) = when {
        score == null -> "No data" to colors.textMuted
        score >= 90 -> "Excellent" to colors.green
        score >= 75 -> "Good" to Color(0xFF6EE7B7)
        score >= 60 -> "Fair" to colors.yellow
        score >= 40 -> "Needs work" to Color(0xFFFB923C)
        else -> "Poor" to colors.red
    }

    LaunchedEffect(score) {
        fraction.snapTo(0f)
        fraction.animateTo(
            targetValue    = (score ?: 0) / 100f,
            animationSpec  = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke     = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val padding    = strokeWidth.toPx() / 2
            val arcSize    = Size(this.size.width - padding * 2, this.size.height - padding * 2)
            val topLeft    = Offset(padding, padding)

            // Track
            drawArc(
                color      = colors.border,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = stroke
            )

            // Progress sweep with gradient
            drawArc(
                brush      = Brush.sweepGradient(
                    colors  = listOf(GradientStart, GradientEnd, GradientStart),
                    center  = Offset(this.size.width / 2, this.size.height / 2)
                ),
                startAngle = 135f,
                sweepAngle = 270f * fraction.value,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = stroke
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = displayScore?.toString() ?: "—",
                fontFamily = DmSerifDisplay,
                fontSize   = 40.sp,
                fontWeight = FontWeight.Normal,
                color      = Color(0xFFE8DFFF)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text       = label,
                fontFamily = DmSans,
                fontSize   = 11.sp,
                color      = colors.textMuted
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(qualityColor, CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = qualityLabel,
                    fontFamily = DmSans,
                    fontSize   = 11.sp,
                    color      = qualityColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
