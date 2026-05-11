package com.circadianx.sleepsense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DayScore(val dayLabel: String, val score: Int?)

/**
 * 7-day sleep score bar chart with a trend arrow.
 * Bars colored by score quality.
 */
@Composable
fun WeeklyBarChart(
    days: List<DayScore>,
    modifier: Modifier = Modifier
) {
    val colors = SleepSenseTheme.colors
    val maxScore = 100f

    // Trend arrow: compare first half avg vs second half avg
    val trend: TrendDirection = run {
        val valid = days.mapNotNull { it.score?.toDouble() }
        if (valid.size < 2) return@run TrendDirection.NEUTRAL
        val half = valid.size / 2
        val first = valid.take(half).average()
        val last  = valid.takeLast(half).average()
        when {
            last > first + 3.0 -> TrendDirection.IMPROVING
            last < first - 3.0 -> TrendDirection.WORSENING
            else               -> TrendDirection.NEUTRAL
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = "7-day sleep score trend",
                fontFamily = JetBrainsMono,
                fontSize   = 11.sp,
                color      = colors.textMuted
            )
            TrendBadge(trend)
        }

        Row(
            modifier              = Modifier.fillMaxWidth().height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment     = Alignment.Bottom
        ) {
            days.forEach { day ->
                Column(
                    modifier            = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (day.score != null) {
                        val fraction = (day.score / maxScore).coerceIn(0.05f, 1f)
                        val barColor = barColorForScore(day.score, colors)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((fraction * 64).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(barColor)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.bgCard)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = day.dayLabel,
                        fontFamily = JetBrainsMono,
                        fontSize   = 9.sp,
                        color      = colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendBadge(trend: TrendDirection) {
    val colors = SleepSenseTheme.colors
    val (arrow, label, color) = when (trend) {
        TrendDirection.IMPROVING -> Triple("↑", "Improving", colors.green)
        TrendDirection.WORSENING -> Triple("↓", "Worsening", colors.red)
        TrendDirection.NEUTRAL   -> Triple("→", "Stable",    colors.textMuted)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = arrow, fontSize = 14.sp, color = color)
        Text(
            text       = label,
            fontFamily = JetBrainsMono,
            fontSize   = 10.sp,
            color      = color
        )
    }
}

enum class TrendDirection { IMPROVING, WORSENING, NEUTRAL }

@Composable
private fun barColorForScore(score: Int, colors: com.circadianx.sleepsense.ui.theme.SleepSenseColors): Color =
    when {
        score >= 80 -> colors.green
        score >= 60 -> colors.yellow
        else -> colors.red
    }

fun buildDayLabels(sessions: List<Pair<Long, Int?>>): List<DayScore> {
    val fmt = SimpleDateFormat("EEE", Locale.getDefault())
    return sessions.map { (ts, ahi) ->
        DayScore(fmt.format(Date(ts)).take(2), ahi)
    }
}
