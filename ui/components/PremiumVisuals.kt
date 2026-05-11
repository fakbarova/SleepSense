package com.circadianx.sleepsense.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.GradientEnd
import com.circadianx.sleepsense.ui.theme.GradientStart
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PremiumSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    accent: Color? = null,
    glow: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = SleepSenseTheme.colors
    val accentColor = accent ?: colors.purple
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = 0.34f),
                        colors.blue.copy(alpha = 0.14f),
                        colors.border
                    )
                )
            )
            .padding(1.dp)
            .clip(RoundedCornerShape(cornerRadius - 1.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.bgElevated.copy(alpha = 0.94f),
                        colors.bgCard.copy(alpha = 0.96f),
                        colors.bgBase.copy(alpha = 0.88f)
                    )
                )
            )
    ) {
        if (glow) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = accentColor.copy(alpha = 0.08f),
                    radius = size.minDimension * 0.58f,
                    center = Offset(size.width * 0.14f, size.height * 0.08f)
                )
                drawCircle(
                    color = colors.blue.copy(alpha = 0.06f),
                    radius = size.minDimension * 0.48f,
                    center = Offset(size.width * 0.96f, size.height * 0.9f)
                )
            }
        }
        content()
    }
}

@Composable
fun TrustChip(
    label: String,
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    val colors = SleepSenseTheme.colors
    val chipColor = color ?: colors.purple
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(chipColor.copy(alpha = 0.12f))
            .border(1.dp, chipColor.copy(alpha = 0.34f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(chipColor)
        )
        Text(
            text = label,
            fontFamily = JetBrainsMono,
            fontSize = 9.sp,
            color = colors.textSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun NightTimelineStrip(
    startTimeMs: Long,
    endTimeMs: Long,
    disturbanceCount: Int,
    modifier: Modifier = Modifier
) {
    val colors = SleepSenseTheme.colors
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Night timeline", fontFamily = JetBrainsMono, fontSize = 9.sp, letterSpacing = 1.sp, color = colors.textMuted)
            Text("${timeFmt.format(Date(startTimeMs))} - ${timeFmt.format(Date(endTimeMs))}", fontFamily = JetBrainsMono, fontSize = 9.sp, color = colors.textMuted)
        }
        Spacer(Modifier.height(10.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
            val y = size.height / 2f
            drawLine(
                color = colors.border,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 10.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 10.dp.toPx(),
                cap = StrokeCap.Round
            )
            repeat(disturbanceCount.coerceIn(0, 8)) { index ->
                val fraction = (index + 1f) / (disturbanceCount.coerceAtLeast(1) + 1f)
                val wobble = if (index % 2 == 0) 0.04f else -0.03f
                val x = ((fraction + wobble).coerceIn(0.08f, 0.92f)) * size.width
                drawCircle(color = colors.red.copy(alpha = 0.34f), radius = 8.dp.toPx(), center = Offset(x, y))
                drawCircle(color = colors.red, radius = 3.dp.toPx(), center = Offset(x, y))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Bedtime", fontFamily = DmSans, fontSize = 10.sp, color = colors.textSecondary)
            Text("${disturbanceCount} disturbance${if (disturbanceCount == 1) "" else "s"}", fontFamily = DmSans, fontSize = 10.sp, color = colors.textSecondary)
            Text("Wake", fontFamily = DmSans, fontSize = 10.sp, color = colors.textSecondary)
        }
    }
}

@Composable
fun AiReportLoadingSequence(modifier: Modifier = Modifier) {
    val colors = SleepSenseTheme.colors
    val steps = listOf("Finding sleep patterns", "Comparing activity", "Building recommendations")
    var activeStep by remember { mutableIntStateOf(0) }
    val transition = rememberInfiniteTransition(label = "report_loading")
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbPulse"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(1150)
            activeStep = (activeStep + 1) % steps.size
        }
    }

    PremiumSurface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        accent = colors.purple
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(132.dp)) {
                Canvas(modifier = Modifier.size(132.dp)) {
                    drawCircle(color = colors.purple.copy(alpha = 0.08f * pulse), radius = size.minDimension / 2f)
                    drawCircle(
                        color = colors.blue.copy(alpha = 0.18f),
                        radius = size.minDimension / 2.8f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(GradientStart, GradientEnd, GradientStart)),
                        startAngle = -90f,
                        sweepAngle = 260f * pulse,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text("AI", fontFamily = JetBrainsMono, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }
            Spacer(Modifier.height(Spacing.l))
            Text(
                text = "Analyzing your sleep fingerprint",
                fontFamily = DmSerifDisplay,
                fontSize = 26.sp,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.s))
            Crossfade(targetState = activeStep, label = "report_step") { index ->
                Text(
                    text = steps[index],
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    color = colors.purple,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(Spacing.l))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .width(if (index == activeStep) 22.dp else 7.dp)
                            .height(7.dp)
                            .clip(CircleShape)
                            .background(if (index == activeStep) colors.purple else colors.border)
                    )
                }
            }
        }
    }
}
