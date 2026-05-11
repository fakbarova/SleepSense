package com.circadianx.sleepsense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.data.network.NightHighlight
import com.circadianx.sleepsense.data.network.ReportResponse
import com.circadianx.sleepsense.ui.components.AiReportLoadingSequence
import com.circadianx.sleepsense.ui.components.PremiumSurface
import com.circadianx.sleepsense.ui.components.SsScoreRing
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.components.TrustChip
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.GradientEnd
import com.circadianx.sleepsense.ui.theme.GradientStart
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.ReportViewModel

@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = SleepSenseTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        SsTopBar(tag = "AI Analysis", title = "Weekly Report")

        when {
            state.isLoading -> LoadingReport()
            state.error != null -> ErrorReport(message = state.error.orEmpty(), onRetry = viewModel::generateReport)
            state.report != null -> ReportContent(report = state.report!!)
        }
    }
}

@Composable
private fun LoadingReport() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AiReportLoadingSequence()
    }
}

@Composable
private fun ErrorReport(message: String, onRetry: () -> Unit) {
    val colors = SleepSenseTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontFamily = DmSans,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = colors.red,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.l))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Try again", fontFamily = DmSans)
        }
    }
}

@Composable
private fun ReportContent(report: ReportResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        HeaderCard(report)
        NarrativeCard(report)
        SectionLabel("Patterns Detected")
        report.patterns.forEachIndexed { index, pattern ->
            InsightCard(index = index + 1, text = pattern)
        }
        SectionLabel("Risk Assessment")
        RiskCard(text = report.riskAssessment)
        SectionLabel("Personalized Tips")
        report.recommendations.forEachIndexed { index, recommendation ->
            NumberedCard(number = index + 1, text = recommendation)
        }
        SectionLabel("Highlights")
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            HighlightCard(label = "Best night", highlight = report.highlights.bestNight, modifier = Modifier.weight(1f))
            HighlightCard(label = "Watch night", highlight = report.highlights.worstNight, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(Spacing.xxl))
    }
}

@Composable
private fun HeaderCard(report: ReportResponse) {
    val colors = SleepSenseTheme.colors
    val delta = report.weeklyScore - report.previousWeekScore
    val deltaLabel = if (delta >= 0) "+$delta vs last week" else "$delta vs last week"
    val trendColor = when (report.trend.lowercase()) {
        "improving" -> colors.green
        "declining" -> colors.red
        else -> colors.yellow
    }

    PremiumSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        accent = trendColor
    ) {
        Column(modifier = Modifier.padding(Spacing.xl)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sleep Intelligence",
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        letterSpacing = 1.4.sp,
                        color = colors.textMuted
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Your week is ${report.trend}",
                        fontFamily = DmSerifDisplay,
                        fontSize = 28.sp,
                        lineHeight = 31.sp,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(Spacing.m))
                    TrendBadge(label = deltaLabel, color = trendColor)
                }
                SsScoreRing(score = report.weeklyScore, size = 120.dp, strokeWidth = 10.dp, label = "Weekly")
            }
            Spacer(Modifier.height(Spacing.l))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                TrustChip(label = "14 nights analyzed", color = colors.blue)
                TrustChip(label = "Confidence high", color = trendColor)
            }
        }
    }
}

@Composable
private fun NarrativeCard(report: ReportResponse) {
    val colors = SleepSenseTheme.colors
    val headline = when (report.trend.lowercase()) {
        "improving" -> "Your recovery trend is moving in the right direction."
        "declining" -> "Your week shows a clear recovery dip worth acting on."
        else -> "Your sleep is steady, and the next gain is consistency."
    }
    PremiumSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        accent = colors.purple,
        glow = false
    ) {
        Column(modifier = Modifier.padding(Spacing.l)) {
            Text(
                text = "THIS WEEK IN ONE SENTENCE",
                fontFamily = JetBrainsMono,
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
                color = colors.purple
            )
            Spacer(Modifier.height(Spacing.s))
            Text(
                text = headline,
                fontFamily = DmSerifDisplay,
                fontSize = 23.sp,
                lineHeight = 27.sp,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(Spacing.s))
            Text(
                text = report.patterns.firstOrNull() ?: "SleepSense found enough signal to build a personalized weekly report.",
                fontFamily = DmSans,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = colors.textSecondary
            )
        }
    }
}
@Composable
private fun TrendBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.42f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = SleepSenseTheme.colors
    Text(
        text = text.uppercase(),
        fontFamily = JetBrainsMono,
        fontSize = 10.sp,
        letterSpacing = 1.4.sp,
        color = colors.textMuted,
        modifier = Modifier.padding(top = Spacing.s, start = 4.dp)
    )
}

@Composable
private fun InsightCard(index: Int, text: String) {
    val colors = SleepSenseTheme.colors
    PremiumSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        accent = colors.blue,
        glow = false
    ) {
        Row(
            modifier = Modifier.padding(Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.Top
        ) {
            Orb(label = "P$index", color = colors.blue)
            Text(text = text, fontFamily = DmSans, fontSize = 14.sp, lineHeight = 21.sp, color = colors.textPrimary)
        }
    }
}

@Composable
private fun RiskCard(text: String) {
    val colors = SleepSenseTheme.colors
    val riskColor = when {
        text.contains("high", ignoreCase = true) -> colors.red
        text.contains("moderate", ignoreCase = true) || text.contains("medium", ignoreCase = true) -> colors.yellow
        else -> colors.green
    }
    TextCard(text = text, borderColor = riskColor, accentColor = riskColor)
}

@Composable
private fun NumberedCard(number: Int, text: String) {
    val colors = SleepSenseTheme.colors
    PremiumSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        accent = colors.purple,
        glow = false
    ) {
        Row(
            modifier = Modifier.padding(Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.Top
        ) {
            Orb(label = number.toString(), color = colors.purple)
            Text(text = text, fontFamily = DmSans, fontSize = 14.sp, lineHeight = 21.sp, color = colors.textPrimary)
        }
    }
}

@Composable
private fun TextCard(text: String, borderColor: Color, accentColor: Color) {
    val colors = SleepSenseTheme.colors
    PremiumSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        accent = borderColor,
        glow = false
    ) {
        Column(modifier = Modifier.padding(Spacing.l)) {
            Text(
                text = "Clinical-style note, not a diagnosis",
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                color = accentColor
            )
            Spacer(Modifier.height(Spacing.s))
            Text(text = text, fontFamily = DmSans, fontSize = 14.sp, lineHeight = 22.sp, color = colors.textPrimary)
        }
    }
}

@Composable
private fun HighlightCard(label: String, highlight: NightHighlight, modifier: Modifier = Modifier) {
    val colors = SleepSenseTheme.colors
    PremiumSurface(
        modifier = modifier,
        cornerRadius = 18.dp,
        accent = if (label.startsWith("Best")) colors.green else colors.yellow,
        glow = false
    ) {
        Column(modifier = Modifier.padding(Spacing.l)) {
            Text(text = label.uppercase(), fontFamily = JetBrainsMono, fontSize = 9.sp, color = colors.textMuted)
            Spacer(Modifier.height(Spacing.s))
            Text(text = "${highlight.score}", fontFamily = DmSerifDisplay, fontSize = 34.sp, color = Color(0xFFE8DFFF))
            Text(text = highlight.date, fontFamily = JetBrainsMono, fontSize = 10.sp, color = colors.purple)
            Spacer(Modifier.height(Spacing.s))
            Text(text = highlight.note, fontFamily = DmSans, fontSize = 12.sp, lineHeight = 17.sp, color = colors.textSecondary)
        }
    }
}

@Composable
private fun Orb(label: String, color: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontFamily = JetBrainsMono, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
    }
}
