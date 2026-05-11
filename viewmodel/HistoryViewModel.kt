package com.circadianx.sleepsense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.data.local.db.dao.SleepRecordDao
import com.circadianx.sleepsense.data.local.db.entity.SleepRecordEntity
import com.circadianx.sleepsense.ui.components.DayScore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HistoryUiState(
    val records: List<SleepRecordEntity> = emptyList(),
    val weekDays: List<DayScore> = emptyList()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sleepRecordDao: SleepRecordDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())

    init {
        viewModelScope.launch {
            sleepRecordDao.observeRecent(limit = 60).collect { records ->
                val weekDays = buildWeekDays(records)
                _uiState.update { it.copy(records = records, weekDays = weekDays) }
            }
        }
    }

    private fun buildWeekDays(records: List<SleepRecordEntity>): List<DayScore> {
        // Map most recent 7 records into DayScore entries
        return records.take(7).reversed().map { r ->
            DayScore(
                dayLabel = dayFmt.format(Date(r.startTimeMs)).take(2),
                score = r.sleepScore
            )
        }.let { list ->
            // Pad to 7 if fewer sessions exist
            val pad = 7 - list.size
            List(pad) { DayScore("–", null) } + list
        }
    }
}
