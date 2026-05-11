package com.circadianx.sleepsense.ui.screens

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.circadianx.sleepsense.presentation.onboarding.OnboardingViewModel
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.util.TimeUtils
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val colors  = SleepSenseTheme.colors
    val context = LocalContext.current
    val state   by viewModel.state.collectAsState()

    if (state.onboardingCompleted) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    var step          by remember { mutableIntStateOf(0) }
    val totalSteps     = 3
    var bedtime       by remember { mutableStateOf<LocalTime?>(null) }
    var wakeTime      by remember { mutableStateOf<LocalTime?>(null) }
    var selectedGoals by remember { mutableStateOf(state.primaryGoals) }
    val timeFormatter  = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val requestNotifications = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Top bar: progress dots + skip ────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.l),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(totalSteps) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == step) 24.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (i < step) colors.purple
                                    else if (i == step) colors.purple
                                    else colors.border
                                )
                        )
                    }
                }
                if (step < totalSteps - 1) {
                    TextButton(onClick = {
                        step++
                    }) {
                        Text(
                            text       = "Skip",
                            fontFamily = DmSans,
                            fontSize   = 13.sp,
                            color      = colors.textMuted
                        )
                    }
                }
            }

            // ── Scrollable content ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(Spacing.l))

                Text(
                    text       = "SleepSense",
                    fontFamily = DmSerifDisplay,
                    fontSize   = 28.sp,
                    color      = colors.purple
                )

                Spacer(Modifier.height(Spacing.xxl))

                when (step) {
                    0 -> ScheduleStep(
                        bedtime      = bedtime,
                        wakeTime     = wakeTime,
                        timeFormatter = timeFormatter,
                        onBedtime    = { time -> bedtime = time },
                        onWakeTime   = { time -> wakeTime = time }
                    )
                    1 -> GoalsStep(
                        selectedGoals = selectedGoals,
                        onToggle      = { goal ->
                            selectedGoals = if (selectedGoals.contains(goal))
                                selectedGoals - goal else selectedGoals + goal
                        }
                    )
                    else -> PermissionsStep(
                        onNotifications = {
                            if (Build.VERSION.SDK_INT >= 33) {
                                requestNotifications.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onUsageAccess = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        },
                        onAccessibility = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    )
                }
                Spacer(Modifier.height(Spacing.xxl))
            }

            // ── Bottom CTA ───────────────────────────────────────────────────
            val canContinue = when (step) {
                0    -> bedtime != null && wakeTime != null
                1    -> selectedGoals.isNotEmpty()
                else -> true
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.l)
            ) {
                Button(
                    onClick = {
                        when (step) {
                            0 -> {
                                val bed  = bedtime  ?: return@Button
                                val wake = wakeTime ?: return@Button
                                viewModel.setSchedule(TimeUtils.toMinutes(bed), TimeUtils.toMinutes(wake))
                                step = 1
                            }
                            1 -> {
                                viewModel.setGoals(selectedGoals)
                                step = 2
                            }
                            else -> {
                                viewModel.completeOnboarding()
                                onFinished()
                            }
                        }
                    },
                    enabled  = canContinue,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = colors.purple)
                ) {
                    Text(
                        text       = if (step == totalSteps - 1) "Finish setup" else "Continue",
                        fontFamily = DmSans,
                        fontWeight = FontWeight.Medium,
                        fontSize   = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleStep(
    bedtime: LocalTime?,
    wakeTime: LocalTime?,
    timeFormatter: DateTimeFormatter,
    onBedtime: (LocalTime) -> Unit,
    onWakeTime: (LocalTime) -> Unit
) {
    val colors  = SleepSenseTheme.colors
    val context = LocalContext.current

    StepHeader(
        title = "Set your sleep schedule",
        body  = "SleepSense uses your target bedtime and wake time to detect sleep automatically."
    )
    Spacer(Modifier.height(Spacing.xl))

    SchedulePickerCard(
        title   = "Target bedtime",
        value   = bedtime?.format(timeFormatter) ?: "Choose time",
        onClick = {
            val initial = bedtime ?: LocalTime.of(23, 0)
            TimePickerDialog(context, { _, h, m -> onBedtime(LocalTime.of(h, m)) },
                initial.hour, initial.minute, true).show()
        }
    )
    Spacer(Modifier.height(Spacing.m))
    SchedulePickerCard(
        title   = "Target wake time",
        value   = wakeTime?.format(timeFormatter) ?: "Choose time",
        onClick = {
            val initial = wakeTime ?: LocalTime.of(7, 0)
            TimePickerDialog(context, { _, h, m -> onWakeTime(LocalTime.of(h, m)) },
                initial.hour, initial.minute, true).show()
        }
    )
}

@Composable
private fun GoalsStep(selectedGoals: Set<String>, onToggle: (String) -> Unit) {
    val goals = listOf(
        "Improve sleep quality",
        "Lose weight",
        "Decrease screen time",
        "Build exercise habit",
        "Reduce/prevent body pain"
    )

    StepHeader(
        title = "Choose your goals",
        body  = "We'll tailor reminders, challenges, and weekly insights to what matters to you."
    )
    Spacer(Modifier.height(Spacing.xl))

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        goals.forEach { goal ->
            GoalRow(
                label   = goal,
                checked = selectedGoals.contains(goal),
                onToggle = { onToggle(goal) }
            )
        }
    }
}

@Composable
private fun PermissionsStep(
    onNotifications: () -> Unit,
    onUsageAccess: () -> Unit,
    onAccessibility: () -> Unit
) {
    StepHeader(
        title = "Enable key permissions",
        body  = "SleepSense works without every permission, but these unlock passive tracking and gentle interventions."
    )
    Spacer(Modifier.height(Spacing.xl))

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        PermissionCard(title = "Notifications", body = "Morning summaries, reminders, and weekly reports.", cta = "Enable", onClick = onNotifications)
        PermissionCard(title = "Usage access", body = "Detect bedtime screen use and generate sleep disturbance logs.", cta = "Open settings", onClick = onUsageAccess)
        PermissionCard(title = "Accessibility (optional)", body = "Bedtime app blocking to reduce late-night scrolling.", cta = "Open settings", onClick = onAccessibility)
    }
}

@Composable
private fun StepHeader(title: String, body: String) {
    val colors = SleepSenseTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = title,
            fontFamily = DmSerifDisplay,
            fontSize   = 26.sp,
            color      = colors.textPrimary,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.m))
        Text(
            text       = body,
            fontFamily = DmSans,
            fontSize   = 15.sp,
            color      = colors.textSecondary,
            textAlign  = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchedulePickerCard(title: String, value: String, onClick: () -> Unit) {
    val colors = SleepSenseTheme.colors
    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = colors.bgCard),
        border   = BorderStroke(1.dp, colors.border),
        shape    = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.l)) {
            Text(
                text          = title.uppercase(),
                fontFamily    = JetBrainsMono,
                fontSize      = 9.sp,
                letterSpacing = 1.2.sp,
                color         = colors.textMuted
            )
            Spacer(Modifier.height(Spacing.s))
            Text(
                text       = value,
                fontFamily = DmSans,
                fontSize   = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (value == "Choose time") colors.textMuted else colors.textPrimary
            )
        }
    }
}

@Composable
private fun GoalRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    val colors = SleepSenseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (checked) colors.purple.copy(alpha = 0.1f) else colors.bgCard)
            .border(
                1.dp,
                if (checked) colors.purple.copy(alpha = 0.4f) else colors.border,
                RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
            .padding(Spacing.l),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = if (checked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (checked) "Selected" else "Not selected",
            tint               = if (checked) colors.purple else colors.textMuted,
            modifier           = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Spacing.m))
        Text(
            text       = label,
            fontFamily = DmSans,
            fontSize   = 15.sp,
            color      = colors.textPrimary,
            fontWeight = if (checked) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionCard(title: String, body: String, cta: String, onClick: () -> Unit) {
    val colors = SleepSenseTheme.colors
    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = colors.bgCard),
        border   = BorderStroke(1.dp, colors.border),
        shape    = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.l)) {
            Text(title, fontFamily = DmSans, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(body, fontFamily = DmSans, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 20.sp)
            Spacer(Modifier.height(Spacing.m))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(colors.purple))
                Spacer(Modifier.width(6.dp))
                Text(cta, fontFamily = JetBrainsMono, fontSize = 11.sp, color = colors.purple, fontWeight = FontWeight.Medium)
            }
        }
    }
}
