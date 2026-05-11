package com.circadianx.sleepsense.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.ui.components.SsEmptyState
import com.circadianx.sleepsense.ui.components.NightTimelineStrip
import com.circadianx.sleepsense.ui.components.PremiumSurface
import com.circadianx.sleepsense.ui.components.SsScoreRing
import com.circadianx.sleepsense.ui.components.SsSparkline
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.components.StatCard
import com.circadianx.sleepsense.ui.components.TrustChip
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(
    onStartRecording: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenReport: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state  by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = SleepSenseTheme.colors
    val context = LocalContext.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startRecording()
            onStartRecording()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required to record sleep.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        SsTopBar(tag = "SleepSense", title = "Good morning")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
        ) {
            Spacer(Modifier.height(Spacing.l))

            if (!state.hasSchedule) {
                SetupPromptCard()
                Spacer(Modifier.height(Spacing.l))
            }

            // ── Hero: score ring + sparkline ─────────────────────────────────
            HeroCard(state = state)

            Spacer(Modifier.height(Spacing.m))

            // ── Stats row ────────────────────────────────────────────────────
            val latest       = state.latestSleep
            val duration     = latest?.let { fmtDuration(it.endTimeMs - it.startTimeMs) } ?: "—"
            val disturbances = latest?.disturbanceCount?.toString() ?: "—"
            val nights       = state.recentSleeps.size.toString()

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                StatCard(
                    value       = duration,
                    label       = "Total sleep",
                    accentColor = colors.blue,
                    modifier    = Modifier.weight(1f)
                )
                StatCard(
                    value              = disturbances,
                    label              = "Disturbances",
                    accentColor        = colors.green,
                    modifier           = Modifier.weight(1f),
                    deltaPositiveIsBad = true
                )
                StatCard(
                    value       = nights,
                    label       = "Nights tracked",
                    accentColor = colors.yellow,
                    modifier    = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(Spacing.l))

            // ── Quick actions ─────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                Button(
                    onClick  = {
                        val granted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            viewModel.startRecording()
                            onStartRecording()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = colors.purple)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Mic,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = "Record",
                        fontFamily = DmSans,
                        fontWeight = FontWeight.Medium,
                        fontSize   = 14.sp
                    )
                }
                OutlinedButton(
                    onClick  = onOpenChat,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, colors.stroke)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.ChatBubble,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp),
                        tint               = colors.purple
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = "Ask AI",
                        fontFamily = DmSans,
                        fontWeight = FontWeight.Medium,
                        fontSize   = 14.sp,
                        color      = colors.purple
                    )
                }
            }

            Spacer(Modifier.height(Spacing.m))

            Button(
                onClick  = onOpenReport,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = colors.blue)
            ) {
                Icon(
                    imageVector        = Icons.Filled.BarChart,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "View AI Report",
                    fontFamily = DmSans,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 14.sp
                )
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun HeroCard(state: com.circadianx.sleepsense.viewmodel.DashboardUiState) {
    val colors = SleepSenseTheme.colors
    val latest = state.latestSleep

    val sparkValues = state.recentSleeps
        .takeLast(7)
        .map { it.sleepScore.toFloat() }
        .reversed()

    PremiumSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        accent = colors.purple
    ) {
        Column(modifier = Modifier.padding(Spacing.l)) {
            if (latest == null) {
                SsEmptyState(
                    icon  = Icons.Filled.NightShelter,
                    title = "No sleep recorded yet",
                    body  = "Start a recording session tonight to see your first sleep score here.",
                    ctaLabel = "Start tonight"
                )
            } else {
                val dateFmt = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text       = "Last night",
                            fontFamily = DmSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 13.sp,
                            color      = colors.textSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text       = dateFmt.format(Date(latest.startTimeMs)),
                            fontFamily = JetBrainsMono,
                            fontSize   = 10.sp,
                            color      = colors.textMuted
                        )
                    }
                    TrustChip(label = "ESP32 synced", color = colors.green)
                }

                Spacer(Modifier.height(Spacing.l))

                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SsScoreRing(
                        score  = latest.sleepScore,
                        size   = 140.dp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(Modifier.width(Spacing.l))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = nightStory(
                                score = latest.sleepScore,
                                durationMs = latest.endTimeMs - latest.startTimeMs,
                                recentScores = state.recentSleeps.map { it.sleepScore }
                            ),
                            fontFamily = DmSerifDisplay,
                            fontSize   = 21.sp,
                            lineHeight = 24.sp,
                            color      = colors.textPrimary
                        )
                        Spacer(Modifier.height(Spacing.m))
                        if (sparkValues.size >= 2) {
                            Text(
                                text       = "7-day trend",
                                fontFamily = JetBrainsMono,
                                fontSize   = 9.sp,
                                letterSpacing = 1.sp,
                                color      = colors.textMuted
                            )
                            Spacer(Modifier.height(6.dp))
                            SsSparkline(
                                values   = sparkValues,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.l))

                NightTimelineStrip(
                    startTimeMs = latest.startTimeMs,
                    endTimeMs = latest.endTimeMs,
                    disturbanceCount = latest.disturbanceCount
                )
            }
        }
    }
}

@Composable
private fun SetupPromptCard() {
    val colors = SleepSenseTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.purple.copy(alpha = 0.1f))
            .border(1.dp, colors.purple.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(Spacing.l)
    ) {
        Text(
            text       = "Set up your sleep schedule",
            fontFamily = DmSans,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 14.sp,
            color      = colors.textPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = "Complete onboarding to enable passive sleep detection — SleepSense will use your bedtime to start tracking automatically.",
            fontFamily = DmSans,
            fontSize   = 12.sp,
            color      = colors.textSecondary,
            lineHeight = 18.sp
        )
    }
}

private fun fmtDuration(ms: Long): String {
    val h = TimeUnit.MILLISECONDS.toHours(ms)
    val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    return "${h}h ${m}m"
}

private fun nightStory(score: Int, durationMs: Long, recentScores: List<Int>): String {
    val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val average = recentScores.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: score
    return when {
        score >= 90 -> "Exceptional recovery night, ${durationMinutes / 60}h ${durationMinutes % 60}m of calm sleep."
        score >= average + 5 -> "Your score beat your recent average by ${score - average} points."
        score < 60 -> "A rough night, but the pattern gives us something clear to improve."
        durationMinutes >= 450 -> "Strong duration, steady recovery, and a healthy sleep window."
        else -> "Shorter than ideal, but your next best night is very reachable."
    }
}
