package com.circadianx.sleepsense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing

@Composable
fun SsTopBar(
    tag: String,
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    val colors = SleepSenseTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bgDeep)
            .statusBarsPadding()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.l)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom
        ) {
            Column {
                Text(
                    text          = tag.uppercase(),
                    fontFamily    = JetBrainsMono,
                    fontSize      = 10.sp,
                    letterSpacing = 1.6.sp,
                    color         = colors.purple
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text       = title,
                    fontFamily = DmSerifDisplay,
                    fontSize   = 24.sp,
                    color      = colors.textPrimary
                )
            }
            actions()
        }
    }
}
