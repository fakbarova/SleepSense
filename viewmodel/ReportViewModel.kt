package com.circadianx.sleepsense.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.data.local.db.dao.SleepRecordDao
import com.circadianx.sleepsense.data.local.db.dao.StepDao
import com.circadianx.sleepsense.data.network.ReportRepository
import com.circadianx.sleepsense.data.network.ReportRequest
import com.circadianx.sleepsense.data.network.ReportResponse
import com.circadianx.sleepsense.data.network.ReportSleepRecord
import com.circadianx.sleepsense.data.preferences.UserPreferences
import com.circadianx.sleepsense.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class ReportUiState(
    val isLoading: Boolean = true,
    val report: ReportResponse? = null,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val sleepRecordDao: SleepRecordDao,
    private val stepDao: StepDao,
    private val prefs: UserPreferencesRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        generateReport()
    }

    fun generateReport() {
        viewModelScope.launch {
            _uiState.value = ReportUiState(isLoading = true)

            val request = try {
                buildRequest()
            } catch (t: Throwable) {
                _uiState.value = ReportUiState(
                    isLoading = false,
                    error = "Couldn't prepare report data.\n(${t.message ?: t.javaClass.simpleName})"
                )
                return@launch
            }

            val result = reportRepository.generateReport(request)
            _uiState.value = result.fold(
                onSuccess = { ReportUiState(isLoading = false, report = it) },
                onFailure = {
                    ReportUiState(
                        isLoading = false,
                        error = "Couldn't generate report. Make sure the backend is running.\n(${it.message})"
                    )
                }
            )
        }
    }

    private suspend fun buildRequest(): ReportRequest {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sleepRecords = sleepRecordDao.observeRecent(14).first().map { record ->
            ReportSleepRecord(
                date = dateFormat.format(Date(record.startTimeMs)),
                score = record.sleepScore,
                durationMinutes = TimeUnit.MILLISECONDS.toMinutes(record.endTimeMs - record.startTimeMs),
                disturbances = record.disturbanceCount
            )
        }
        val steps = stepDao.observeRecent(14).first().map { it.steps }
        val goals = prefs.primaryGoals.first().toList()
        val userName = dataStore.data.first()[UserPreferences.NAME] ?: "User"

        return ReportRequest(
            sleepRecords = sleepRecords,
            steps = steps,
            goals = goals,
            userName = userName
        )
    }
}
