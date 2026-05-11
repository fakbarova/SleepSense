package com.circadianx.sleepsense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.ui.components.SsEmptyState
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.HabitsViewModel
import com.circadianx.sleepsense.viewmodel.RoutineItemUi

@Composable
fun HabitsScreen(
    onOpenChallenges: () -> Unit = {},
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val state  by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = SleepSenseTheme.colors

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tonight", "Morning")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        SsTopBar(tag = "Routines", title = "Today")

        // Tab row
        TabRow(
            selectedTabIndex   = selectedTab,
            containerColor     = colors.bgDeep,
            contentColor       = colors.purple,
            indicator          = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)),
                    color    = colors.purple
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick  = { selectedTab = index },
                    text     = {
                        Text(
                            text       = tab,
                            fontFamily = DmSans,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize   = 14.sp,
                            color      = if (selectedTab == index) colors.purple else colors.textMuted
                        )
                    }
                )
            }
        }

        val items = if (selectedTab == 0) state.preSleep else state.morning
        val completedCount = items.count { it.completedToday }

        LazyColumn(
            modifier        = Modifier.fillMaxSize(),
            contentPadding  = PaddingValues(
                horizontal = Spacing.screenHorizontal,
                vertical   = Spacing.l
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            // Progress header
            if (items.isNotEmpty()) {
                item {
                    ProgressHeader(completed = completedCount, total = items.size)
                    Spacer(Modifier.height(Spacing.s))
                }
            }

            if (items.isEmpty()) {
                item {
                    SsEmptyState(
                        icon  = Icons.Filled.EmojiObjects,
                        title = "No routines yet",
                        body  = "Routine items will appear here once they are set up."
                    )
                }
            } else {
                items(items, key = { it.id }) { item ->
                    RoutineRow(item = item, onToggle = { viewModel.toggleCompleted(item) })
                }
            }

            item {
                Spacer(Modifier.height(Spacing.l))
                Button(
                    onClick  = onOpenChallenges,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = colors.bgCard
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Flag,
                        contentDescription = null,
                        tint               = colors.purple,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "View Challenges",
                        fontFamily = DmSans,
                        fontWeight = FontWeight.Medium,
                        fontSize   = 14.sp,
                        color      = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(completed: Int, total: Int) {
    val colors   = SleepSenseTheme.colors
    val fraction = if (total == 0) 0f else completed.toFloat() / total

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text       = "$completed / $total complete",
                fontFamily = DmSans,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = colors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.purple)
                )
            }
        }

        Text(
            text       = "${(fraction * 100).toInt()}%",
            fontFamily = JetBrainsMono,
            fontSize   = 13.sp,
            color      = colors.purple,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RoutineRow(item: RoutineItemUi, onToggle: () -> Unit) {
    val colors = SleepSenseTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .clickable { onToggle() }
            .padding(Spacing.l),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = if (item.completedToday)
                Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (item.completedToday) "Completed" else "Not completed",
            tint               = if (item.completedToday) colors.green else colors.textMuted,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(Spacing.m))
        Text(
            text       = item.title,
            fontFamily = DmSans,
            fontSize   = 15.sp,
            color      = if (item.completedToday) colors.textMuted else colors.textPrimary,
            fontWeight = if (item.completedToday) FontWeight.Normal else FontWeight.Medium
        )
    }
}
