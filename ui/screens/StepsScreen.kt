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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.data.local.db.dao.StepDao
import com.circadianx.sleepsense.ui.components.SsSparkline
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.GradientEnd
import com.circadianx.sleepsense.ui.theme.GradientStart
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StepsUiState(
    val todaySteps: Int = 0,
    val weekAvg: Int = 0,
    val weekHistory: List<Float> = emptyList()
)

@HiltViewModel
class StepsViewModel @Inject constructor(stepDao: StepDao) : androidx.lifecycle.ViewModel() {
    val state: StateFlow<StepsUiState> =
        stepDao.observeRecent(7)
            .map { list ->
                val today   = list.firstOrNull()?.steps ?: 0
                val avg     = if (list.isEmpty()) 0 else list.map { it.steps }.average().toInt()
                val history = list.map { it.steps.toFloat() }.reversed()
                StepsUiState(todaySteps = today, weekAvg = avg, weekHistory = history)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StepsUiState())
}

private const val DAILY_GOAL = 8_000

@Composable
fun StepsScreen(viewModel: StepsViewModel = hiltViewModel()) {
    val state  by viewModel.state.collectAsStateWithLifecycle()
    val colors = SleepSenseTheme.colors
    val goal   = DAILY_GOAL
    val pct    = (state.todaySteps.toFloat() / goal).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        SsTopBar(tag = "Activity", title = "Steps")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            // Hero card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(GradientStart.copy(0.12f), GradientEnd.copy(0.12f))
                        )
                    )
                    .border(1.dp, colors.stroke, RoundedCornerShape(20.dp))
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier         = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(colors.purple.copy(alpha = 0.15f))
                        .border(2.dp, colors.purple.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint               = colors.purple,
                        modifier           = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(Spacing.l))

                Text(
                    text       = "%,d".format(state.todaySteps),
                    fontFamily = DmSerifDisplay,
                    fontSize   = 48.sp,
                    color      = colors.textPrimary
                )
                Text(
                    text       = "steps today",
                    fontFamily = DmSans,
                    fontSize   = 13.sp,
                    color      = colors.textMuted
                )

                Spacer(Modifier.height(Spacing.l))

                // Goal progress bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text       = "Daily goal",
                            fontFamily = JetBrainsMono,
                            fontSize   = 10.sp,
                            color      = colors.textMuted
                        )
                        Text(
                            text       = "%,d steps".format(goal),
                            fontFamily = JetBrainsMono,
                            fontSize   = 10.sp,
                            color      = colors.textMuted
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.border)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                                )
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "${(pct * 100).toInt()}% of daily goal",
                        fontFamily = DmSans,
                        fontSize   = 11.sp,
                        color      = colors.textSecondary
                    )
                }
            }

            // Weekly average card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.bgCard)
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(Spacing.l)
            ) {
                Text(
                    text          = "7-DAY AVERAGE",
                    fontFamily    = JetBrainsMono,
                    fontSize      = 9.sp,
                    letterSpacing = 1.2.sp,
                    color         = colors.textMuted
                )
                Spacer(Modifier.height(Spacing.s))
                Text(
                    text       = "%,d".format(state.weekAvg),
                    fontFamily = DmSerifDisplay,
                    fontSize   = 28.sp,
                    color      = colors.textPrimary,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text       = "steps / day",
                    fontFamily = DmSans,
                    fontSize   = 12.sp,
                    color      = colors.textMuted
                )
                if (state.weekHistory.size >= 2) {
                    Spacer(Modifier.height(Spacing.m))
                    SsSparkline(
                        values   = state.weekHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                }
            }
        }
    }
}
