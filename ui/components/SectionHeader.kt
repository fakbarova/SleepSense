package com.circadianx.sleepsense.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme

@Composable
fun SectionHeader(tag: String, title: String, modifier: Modifier = Modifier) {
    val colors = SleepSenseTheme.colors
    Column(modifier = modifier) {
        Text(
            text          = tag.uppercase(),
            fontFamily    = JetBrainsMono,
            fontSize      = 10.sp,
            letterSpacing = 1.6.sp,
            color         = colors.purple
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = title,
            fontFamily = DmSerifDisplay,
            fontWeight = FontWeight.Normal,
            fontSize   = 24.sp,
            color      = colors.textPrimary
        )
    }
}
