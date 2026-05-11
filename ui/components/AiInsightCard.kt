package com.circadianx.sleepsense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.Purple
import com.circadianx.sleepsense.ui.theme.Blue
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme

/**
 * The AI Insight card shown each morning below the AHI ring.
 * Purple gradient background, top border accent.
 */
@Composable
fun AiInsightCard(
    headline: String,
    body: String,
    modifier: Modifier = Modifier
) {
    val colors = SleepSenseTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Purple.copy(alpha = 0.08f),
                        Blue.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Purple.copy(alpha = 0.20f),
                shape = RoundedCornerShape(14.dp)
            )
            // top accent bar via custom border replacement via padding + nested bg
            .padding(top = 2.dp)
            .background(
                Brush.verticalGradient(
                    0f to Purple,
                    0.02f to Purple.copy(alpha = 0f),
                    startY = 0f, endY = 100f
                )
            )
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Text(
            text           = "AI INSIGHT",
            fontFamily     = JetBrainsMono,
            fontSize       = 10.sp,
            letterSpacing  = 1.sp,
            color          = colors.purple
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text       = headline,
            fontFamily = DmSans,
            fontWeight = FontWeight.Medium,
            fontSize   = 15.sp,
            color      = colors.textPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text       = body,
            fontFamily = DmSans,
            fontSize   = 13.sp,
            color      = colors.textSecondary,
            lineHeight = 20.sp
        )
    }
}
