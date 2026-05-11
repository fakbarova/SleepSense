package com.circadianx.sleepsense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.data.local.db.entity.ChallengeEntity
import com.circadianx.sleepsense.ui.components.SsEmptyState
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.ChallengesViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(viewModel: ChallengesViewModel = hiltViewModel()) {
    val state     by viewModel.uiState.collectAsStateWithLifecycle()
    val colors    = SleepSenseTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.bgDeep,
        floatingActionButton = {
            FloatingActionButton(
                onClick            = { showSheet = true },
                containerColor     = colors.purple,
                contentColor       = androidx.compose.ui.graphics.Color.White,
                modifier           = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create challenge")
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bgDeep)
                .padding(inner)
        ) {
            SsTopBar(tag = "Challenges", title = "Commit to a goal")

            LazyColumn(
                modifier        = Modifier.fillMaxSize(),
                contentPadding  = PaddingValues(
                    horizontal = Spacing.screenHorizontal,
                    vertical   = Spacing.l
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                if (state.challenges.isEmpty()) {
                    item {
                        SsEmptyState(
                            icon     = Icons.Filled.Flag,
                            title    = "No challenges yet",
                            body     = "Tap the + button to commit to a personal goal.",
                            ctaLabel = "Create challenge",
                            onCta    = { showSheet = true }
                        )
                    }
                } else {
                    item {
                        Text(
                            text          = "Active",
                            fontFamily    = JetBrainsMono,
                            fontSize      = 10.sp,
                            letterSpacing = 1.4.sp,
                            color         = colors.textMuted
                        )
                    }
                    items(state.challenges, key = { it.id }) { challenge ->
                        ChallengeCard(challenge)
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState       = sheetState,
            containerColor   = colors.bgCard,
            dragHandle       = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.border)
                )
            }
        ) {
            CreateChallengeContent(
                onDismiss = { showSheet = false },
                onCreate  = { title, category, days, criteria ->
                    viewModel.createChallenge(title, category, days, criteria)
                    showSheet = false
                }
            )
        }
    }
}

@Composable
private fun ChallengeCard(challenge: ChallengeEntity) {
    val colors = SleepSenseTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(Spacing.l)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top
        ) {
            Text(
                text       = challenge.title,
                fontFamily = DmSans,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                color      = colors.textPrimary,
                modifier   = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            // Category chip
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.purple.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text       = challenge.category.replace('_', ' ').lowercase(),
                    fontFamily = JetBrainsMono,
                    fontSize   = 9.sp,
                    color      = colors.purple
                )
            }
        }
        Spacer(Modifier.height(Spacing.s))
        Text(
            text       = challenge.successCriteria,
            fontFamily = DmSans,
            fontSize   = 13.sp,
            color      = colors.textSecondary,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(Spacing.m))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text          = "${challenge.durationDays} DAYS",
                fontFamily    = JetBrainsMono,
                fontSize      = 9.sp,
                letterSpacing = 0.8.sp,
                color         = colors.textMuted
            )
            Spacer(Modifier.width(Spacing.m))
            // Placeholder progress bar (no checkins wired yet)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(colors.border)
            )
        }
    }
}

@Composable
private fun CreateChallengeContent(
    onDismiss: () -> Unit,
    onCreate: (String, String, Int, String) -> Unit
) {
    val colors   = SleepSenseTheme.colors
    var title    by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("sleep") }
    var days     by remember { mutableStateOf("30") }
    var criteria by remember { mutableStateOf("Sleep before target bedtime at least 5/7 days") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = colors.purple,
        unfocusedBorderColor = colors.border,
        focusedLabelColor    = colors.purple,
        unfocusedLabelColor  = colors.textMuted,
        focusedTextColor     = colors.textPrimary,
        unfocusedTextColor   = colors.textPrimary,
        cursorColor          = colors.purple
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Text(
            text       = "New Challenge",
            fontFamily = DmSans,
            fontWeight = FontWeight.Bold,
            fontSize   = 18.sp,
            color      = colors.textPrimary
        )
        Spacer(Modifier.height(Spacing.xs))

        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Title", fontFamily = DmSans) },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )
        OutlinedTextField(
            value = category, onValueChange = { category = it },
            label = { Text("Category (sleep / exercise / …)", fontFamily = DmSans) },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )
        OutlinedTextField(
            value = days,
            onValueChange = { days = it.filter { ch -> ch.isDigit() }.take(2) },
            label = { Text("Duration (days)", fontFamily = DmSans) },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )
        OutlinedTextField(
            value = criteria, onValueChange = { criteria = it },
            label = { Text("Success criteria", fontFamily = DmSans) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            maxLines = 3,
            colors = fieldColors
        )

        Spacer(Modifier.height(Spacing.s))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            TextButton(
                onClick   = onDismiss,
                modifier  = Modifier.weight(1f).height(48.dp)
            ) {
                Text("Cancel", fontFamily = DmSans, color = colors.textMuted)
            }
            Button(
                onClick  = { onCreate(title, category, days.toIntOrNull() ?: 30, criteria) },
                modifier = Modifier.weight(1f).height(48.dp),
                enabled  = title.trim().isNotEmpty(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = colors.purple)
            ) {
                Text("Create", fontFamily = DmSans, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(Spacing.l))
    }
}
