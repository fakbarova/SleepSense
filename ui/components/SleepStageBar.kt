package com.circadianx.sleepsense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme

enum class SleepStage { AWAKE, LIGHT, REM, DEEP }

data class SleepSegment(val stage: SleepStage, val weightFraction: Float)

/** Horizontal color bar representing sleep stages across the night. */
@Composable
fun SleepStageBar(
    segments: List<SleepSegment>,
    modifier: Modifier = Modifier
) {
    val colors = SleepSenseTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
            .clip(RoundedCornerShape(8.dp)),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        segments.forEach { seg ->
            val color = when (seg.stage) {
                SleepStage.AWAKE -> colors.bgDeep
                SleepStage.LIGHT -> colors.sleepLight
                SleepStage.REM   -> colors.sleepRem
                SleepStage.DEEP  -> colors.sleepDeep
            }
            Box(
                modifier = Modifier
                    .weight(seg.weightFraction)
                    .height(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

/** Legend row shown below the sleep bar */
@Composable
fun SleepStageLegend(modifier: Modifier = Modifier) {
    val colors = SleepSenseTheme.colors
    Row(
        modifier  = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LegendItem(color = colors.sleepLight, label = "Light")
        LegendItem(color = colors.sleepRem,   label = "REM")
        LegendItem(color = colors.sleepDeep,  label = "Deep")
        LegendItem(color = colors.bgDeep,     label = "Awake", outlined = true)
    }
}

@Composable
private fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    outlined: Boolean = false
) {
    val colors = SleepSenseTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (outlined) Modifier.background(colors.bgDeep) else Modifier
                )
        )
        Text(
            text       = label,
            fontFamily = JetBrainsMono,
            fontSize   = 10.sp,
            color      = colors.textSecondary
        )
    }
}

// Default demo segments matching the HTML design
val defaultSleepSegments = listOf(
    SleepSegment(SleepStage.AWAKE, 0.08f),
    SleepSegment(SleepStage.LIGHT, 0.14f),
    SleepSegment(SleepStage.DEEP,  0.11f),
    SleepSegment(SleepStage.REM,   0.09f),
    SleepSegment(SleepStage.DEEP,  0.13f),
    SleepSegment(SleepStage.LIGHT, 0.11f),
    SleepSegment(SleepStage.REM,   0.08f),
    SleepSegment(SleepStage.LIGHT, 0.12f),
    SleepSegment(SleepStage.REM,   0.08f),
    SleepSegment(SleepStage.AWAKE, 0.06f)
)
