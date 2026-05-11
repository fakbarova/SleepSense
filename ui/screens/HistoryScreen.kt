package com.circadianx.sleepsense.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.data.local.db.entity.SleepRecordEntity
import com.circadianx.sleepsense.ui.components.SsEmptyState
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.components.WeeklyBarChart
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.GradientEnd
import com.circadianx.sleepsense.ui.theme.GradientStart
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private enum class RangeFilter(val label: String, val days: Int) {
    Week("7d", 7), Month("30d", 30), Quarter("90d", 90)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state  by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = SleepSenseTheme.colors

    var range    by remember { mutableStateOf(RangeFilter.Week) }
    var selected by remember { mutableStateOf<SleepRecordEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val records = when (range) {
        RangeFilter.Week    -> state.records.take(7)
        RangeFilter.Month   -> state.records.take(30)
        RangeFilter.Quarter -> state.records.take(90)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        SsTopBar(tag = "Sleep", title = "History")

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
            contentPadding      = androidx.compose.foundation.layout.PaddingValues(
                horizontal = Spacing.screenHorizontal,
                vertical   = Spacing.l
            )
        ) {
            // Range chips
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    RangeFilter.entries.forEach { r ->
                        FilterChip(
                            selected = range == r,
                            onClick  = { range = r },
                            label    = {
                                Text(
                                    r.label,
                                    fontFamily = JetBrainsMono,
                                    fontSize   = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor     = colors.purple.copy(alpha = 0.2f),
                                selectedLabelColor         = colors.purple,
                                containerColor             = colors.bgCard,
                                labelColor                 = colors.textMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled          = true,
                                selected         = range == r,
                                selectedBorderColor = colors.purple.copy(alpha = 0.5f),
                                borderColor      = colors.border
                            )
                        )
                    }
                }
            }

            // Weekly bar chart
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.bgCard)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(Spacing.l)
                ) {
                    WeeklyBarChart(days = state.weekDays)
                }
            }

            if (records.isEmpty()) {
                item {
                    SsEmptyState(
                        icon  = Icons.Filled.Bedtime,
                        title = "No nights tracked yet",
                        body  = "Start a recording session tonight — your sleep history will appear here."
                    )
                }
            } else {
                item {
                    Text(
                        text          = "Sessions",
                        fontFamily    = JetBrainsMono,
                        fontSize      = 10.sp,
                        letterSpacing = 1.4.sp,
                        color         = colors.textMuted
                    )
                }
                items(records, key = { it.id }) { record ->
                    SessionCard(record = record, onClick = { selected = record })
                }
            }

            item { Spacer(Modifier.height(Spacing.l)) }
        }
    }

    if (selected != null) {
        ModalBottomSheet(
            onDismissRequest   = { selected = null },
            sheetState         = sheetState,
            containerColor     = colors.bgCard,
            dragHandle         = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.border)
                )
            }
        ) {
            selected?.let { SessionDetailSheet(it) }
        }
    }
}

@Composable
private fun SessionCard(record: SleepRecordEntity, onClick: () -> Unit) {
    val colors  = SleepSenseTheme.colors
    val dateFmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    val scoreColor = when {
        record.sleepScore >= 80 -> colors.green
        record.sleepScore >= 60 -> colors.yellow
        else                    -> colors.red
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(Spacing.l),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        // Score bubble
        Box(
            modifier        = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(listOf(GradientStart.copy(0.2f), GradientEnd.copy(0.2f)))
                )
                .border(1.dp, colors.stroke, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = record.sleepScore.toString(),
                fontFamily = JetBrainsMono,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = scoreColor
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = dateFmt.format(Date(record.startTimeMs)),
                fontFamily = DmSans,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = colors.textPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text       = "${fmtDuration(record.endTimeMs - record.startTimeMs)} · ${record.disturbanceCount} disturbance(s)",
                fontFamily = DmSans,
                fontSize   = 12.sp,
                color      = colors.textSecondary
            )
        }

        Text(
            text       = "›",
            fontSize   = 18.sp,
            color      = colors.textMuted
        )
    }
}

@Composable
private fun SessionDetailSheet(record: SleepRecordEntity) {
    val colors  = SleepSenseTheme.colors
    val dateFmt = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.l)
    ) {
        Text(
            text       = dateFmt.format(Date(record.startTimeMs)),
            fontFamily = DmSans,
            fontWeight = FontWeight.Bold,
            fontSize   = 18.sp,
            color      = colors.textPrimary
        )
        Spacer(Modifier.height(Spacing.l))
        HorizontalDivider(color = colors.border)
        Spacer(Modifier.height(Spacing.l))

        SheetRow("Sleep score", "${record.sleepScore} / 100")
        SheetRow("Duration", fmtDuration(record.endTimeMs - record.startTimeMs))
        SheetRow("Fell asleep", timeFmt.format(Date(record.startTimeMs)))
        SheetRow("Woke up", timeFmt.format(Date(record.endTimeMs)))
        SheetRow("Disturbances", record.disturbanceCount.toString())

        Spacer(Modifier.height(Spacing.xxl))
    }
}

@Composable
private fun SheetRow(label: String, value: String) {
    val colors = SleepSenseTheme.colors
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = DmSans, fontSize = 14.sp, color = colors.textSecondary)
        Text(value, fontFamily = JetBrainsMono, fontSize = 13.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
    }
}

private fun fmtDuration(ms: Long): String {
    val h = TimeUnit.MILLISECONDS.toHours(ms)
    val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    return "${h}h ${m}m"
}
