package com.circadianx.sleepsense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    onOpenSteps: () -> Unit = {},
    onOpenProgress: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenChallenges: () -> Unit = {},
    onOpenSpotify: () -> Unit = {},
    onSignedOut: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val colors               = SleepSenseTheme.colors
    val userName             by viewModel.userName.collectAsStateWithLifecycle()
    val threshold            by viewModel.thresholdDbfs.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val demoSeedStatus       by viewModel.demoSeedStatus.collectAsStateWithLifecycle()
    val authEmail            by viewModel.authEmail.collectAsStateWithLifecycle()
    val signedIn             by viewModel.signedIn.collectAsStateWithLifecycle()

    var nameInput by remember(userName) { mutableStateOf(userName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        SsTopBar(tag = "Preferences", title = "Profile")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {

            // ── Account ──────────────────────────────────────────────────────
            SectionLabel("Account")
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                    OutlinedTextField(
                        value         = nameInput,
                        onValueChange = { nameInput = it; viewModel.setName(it) },
                        label         = { Text("Your name", fontFamily = DmSans, fontSize = 13.sp) },
                        leadingIcon   = {
                            Icon(Icons.Filled.Person, null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
                        },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = colors.purple,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor    = colors.purple,
                            unfocusedLabelColor  = colors.textMuted,
                            focusedTextColor     = colors.textPrimary,
                            unfocusedTextColor   = colors.textPrimary,
                            cursorColor          = colors.purple
                        )
                    )
                    if (!authEmail.isNullOrBlank()) {
                        Text(
                            text = "Signed in as $authEmail",
                            fontFamily = DmSans,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                    if (signedIn) {
                        TextButton(
                            onClick = {
                                viewModel.signOut()
                                onSignedOut()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign out", fontFamily = DmSans)
                        }
                    }
                }
            }

            // ── Explore ───────────────────────────────────────────────────────
            SectionLabel("Explore")
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    NavigationRow(
                        icon  = Icons.Filled.DirectionsWalk,
                        label = "Steps",
                        sub   = "Daily step count & 7-day average",
                        onClick = onOpenSteps
                    )
                    HorizontalDivider(color = colors.border, modifier = Modifier.padding(start = 48.dp))
                    NavigationRow(
                        icon  = Icons.Filled.CameraAlt,
                        label = "Progress Photos",
                        sub   = "Your encrypted wellness gallery",
                        onClick = onOpenProgress
                    )
                    HorizontalDivider(color = colors.border, modifier = Modifier.padding(start = 48.dp))
                    NavigationRow(
                        icon  = Icons.AutoMirrored.Filled.Chat,
                        label = "AI Chat",
                        sub   = "Ask SleepSense anything",
                        onClick = onOpenChat
                    )
                    HorizontalDivider(color = colors.border, modifier = Modifier.padding(start = 48.dp))
                    NavigationRow(
                        icon = Icons.Filled.Flag,
                        label = "Challenges",
                        sub = "Commit to your habits goals",
                        onClick = onOpenChallenges
                    )
                    HorizontalDivider(color = colors.border, modifier = Modifier.padding(start = 48.dp))
                    NavigationRow(
                        icon = Icons.Filled.MusicNote,
                        label = "Spotify",
                        sub = "Connect music for wind-down routines",
                        onClick = onOpenSpotify
                    )
                }
            }

            // ── Sensors ───────────────────────────────────────────────────────
            SectionLabel("Sensors")
            SettingsCard {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Mic, null, tint = colors.purple, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(Spacing.m))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text       = "Snore sensitivity",
                                fontFamily = DmSans,
                                fontSize   = 14.sp,
                                color      = colors.textPrimary
                            )
                            Text(
                                text       = "${threshold.roundToInt()} dBFS",
                                fontFamily = JetBrainsMono,
                                fontSize   = 11.sp,
                                color      = colors.purple,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Slider(
                            value         = threshold,
                            onValueChange = { viewModel.setThreshold(it) },
                            valueRange    = -40f..-10f,
                            colors        = SliderDefaults.colors(
                                thumbColor         = colors.purple,
                                activeTrackColor   = colors.purple,
                                inactiveTrackColor = colors.border
                            )
                        )
                        Text(
                            text       = "Lower = more sensitive. Higher = louder sounds only.",
                            fontFamily = DmSans,
                            fontSize   = 11.sp,
                            color      = colors.textMuted
                        )
                    }
                }
            }

            // ── Notifications ──────────────────────────────────────────────���──
            SectionLabel("Notifications")
            SettingsCard {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        modifier          = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Notifications, null, tint = colors.purple, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(Spacing.m))
                        Column {
                            Text(
                                text       = "Morning summary",
                                fontFamily = DmSans,
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color      = colors.textPrimary
                            )
                            Text(
                                text       = "Daily notification after recording stops",
                                fontFamily = DmSans,
                                fontSize   = 12.sp,
                                color      = colors.textMuted
                            )
                        }
                    }
                    Switch(
                        checked         = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor   = colors.bgDeep,
                            checkedTrackColor   = colors.purple,
                            uncheckedThumbColor = colors.textMuted,
                            uncheckedTrackColor = colors.border
                        )
                    )
                }
            }

            // ── About ─────────────────────────────────────────────────────────
            SectionLabel("About")
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    InfoRow(label = "App", value = "SleepSense v1.0")
                    HorizontalDivider(color = colors.border)
                    InfoRow(label = "Team", value = "CircadianX")
                    HorizontalDivider(color = colors.border)
                    InfoRow(label = "Recording", value = "Phone microphone")
                    HorizontalDivider(color = colors.border)
                    InfoRow(label = "Hardware", value = "ESP32 via Bluetooth")
                    if (viewModel.canSeedDemoData) {
                        HorizontalDivider(color = colors.border)
                        NavigationRow(
                            icon = Icons.Filled.Info,
                            label = "Load demo data",
                            sub = demoSeedStatus ?: "Populate 14 nights, steps, habits, and stories",
                            onClick = { viewModel.seedDemoData() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = SleepSenseTheme.colors
    Text(
        text          = text.uppercase(),
        fontFamily    = JetBrainsMono,
        fontSize      = 10.sp,
        letterSpacing = 1.4.sp,
        color         = colors.textMuted,
        modifier      = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    val colors = SleepSenseTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgCard)
            .padding(Spacing.l)
    ) {
        content()
    }
}

@Composable
private fun NavigationRow(
    icon: ImageVector,
    label: String,
    sub: String,
    onClick: () -> Unit
) {
    val colors = SleepSenseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Icon(icon, null, tint = colors.purple, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = colors.textPrimary)
            Text(sub, fontFamily = DmSans, fontSize = 12.sp, color = colors.textMuted)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = SleepSenseTheme.colors
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = JetBrainsMono, fontSize = 11.sp, color = colors.textMuted)
        Text(value, fontFamily = DmSans, fontSize = 13.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
    }
}
