package com.circadianx.sleepsense.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing

@Composable
fun SsEmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    ctaLabel: String? = null,
    onCta: (() -> Unit)? = null
) {
    val colors = SleepSenseTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(48.dp),
            tint               = colors.purple.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(Spacing.l))
        Text(
            text       = title,
            fontFamily = DmSans,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 16.sp,
            color      = colors.textPrimary,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.s))
        Text(
            text       = body,
            fontFamily = DmSans,
            fontSize   = 13.sp,
            color      = colors.textMuted,
            textAlign  = TextAlign.Center,
            lineHeight = 20.sp
        )
        if (ctaLabel != null && onCta != null) {
            Spacer(Modifier.height(Spacing.l))
            Button(
                onClick = onCta,
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = colors.purple)
            ) {
                Text(ctaLabel, fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
        }
    }
}
