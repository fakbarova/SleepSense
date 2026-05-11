package com.circadianx.sleepsense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme

/**
 * Stat card with a colored bottom accent bar, value, label, and delta.
 * Mirrors the 4-stat grid in the HTML design.
 */
@Composable
fun StatCard(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    delta: String? = null,
    deltaPositiveIsBad: Boolean = false
) {
    val colors = SleepSenseTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier           = Modifier.padding(16.dp, 16.dp, 16.dp, 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = value,
                fontFamily = DmSerifDisplay,
                fontWeight = FontWeight.Normal,
                fontSize   = if (value.length <= 4) 28.sp else 20.sp,
                color      = colors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = label,
                fontFamily = DmSans,
                fontSize   = 11.sp,
                color      = colors.textSecondary
            )
            if (delta != null) {
                Spacer(Modifier.height(4.dp))
                val isUp = delta.startsWith("↑")
                val deltaColor = when {
                    isUp && deltaPositiveIsBad  -> colors.red
                    isUp && !deltaPositiveIsBad -> colors.green
                    !isUp && deltaPositiveIsBad -> colors.green
                    else                        -> colors.red
                }
                Text(
                    text       = delta,
                    fontFamily = JetBrainsMono,
                    fontSize   = 10.sp,
                    color      = deltaColor
                )
            }
        }
        // Bottom accent bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                .background(accentColor)
        )
    }
}
