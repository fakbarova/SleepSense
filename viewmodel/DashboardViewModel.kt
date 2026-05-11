package com.circadianx.sleepsense.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.data.audio.MicrophoneRecorder
import com.circadianx.sleepsense.data.local.db.dao.SleepRecordDao
import com.circadianx.sleepsense.data.local.db.entity.SleepRecordEntity
import com.circadianx.sleepsense.domain.repository.UserPreferencesRepository
import com.circadianx.sleepsense.data.model.ApneaEvent
import com.circadianx.sleepsense.data.model.SleepSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val latestSleep: SleepRecordEntity? = null,
    val recentSleeps: List<SleepRecordEntity> = emptyList(),
    val hasSchedule: Boolean = false,
    val liveSnoreDb: Float? = null,
    val session: SleepSession? = null,
    val recentEvents: List<ApneaEvent> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sleepRecordDao: SleepRecordDao,
    private val prefs: UserPreferencesRepository,
    private val microphoneRecorder: MicrophoneRecorder
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _liveDbfs = MutableStateFlow(-60f)
    val liveDbfs: StateFlow<Float> = _liveDbfs.asStateFlow()

    private var recordingStartMs: Long? = null
    private var recordingJob: Job? = null
    private var levelJob: Job? = null
    private var disturbanceCount = 0
    private var lastDisturbanceMs = 0L

    init {
        viewModelScope.launch {
            prefs.bedtimeSchedule.collectLatest { schedule ->
                _uiState.update { it.copy(hasSchedule = schedule != null) }
            }
        }
        viewModelScope.launch {
            sleepRecordDao.observeRecent(limit = 14).collectLatest { records ->
                _uiState.update {
                    it.copy(
                        recentSleeps = records,
                        latestSleep = records.firstOrNull()
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (recordingJob?.isActive == true) return

        val startMs = System.currentTimeMillis()
        recordingStartMs = startMs
        disturbanceCount = 0
        lastDisturbanceMs = 0L

        levelJob = viewModelScope.launch {
            microphoneRecorder.liveDbfs.collectLatest { dbfs ->
                _liveDbfs.value = dbfs
                _uiState.update { it.copy(liveSnoreDb = dbfs.takeIf { value -> value > RECORDING_THRESHOLD_DBFS }) }

                val now = System.currentTimeMillis()
                if (dbfs > RECORDING_THRESHOLD_DBFS && now - lastDisturbanceMs > DISTURBANCE_COOLDOWN_MS) {
                    disturbanceCount += 1
                    lastDisturbanceMs = now
                }
            }
        }

        recordingJob = viewModelScope.launch {
            try {
                microphoneRecorder.recordAndDetect(sessionId = startMs, thresholdDbfs = RECORDING_THRESHOLD_DBFS)
                    .collect { packet ->
                        if (packet is com.circadianx.sleepsense.data.bluetooth.HardwarePacket.Apnea) {
                            disturbanceCount += 1
                        }
                    }
            } catch (t: Throwable) {
                // RECORD_AUDIO not granted, mic in use by another app, etc.
                Log.w("DashboardViewModel", "Recording failed", t)
                microphoneRecorder.stop()
                _liveDbfs.value = -60f
            }
        }
    }

    fun stopRecording() {
        val startMs = recordingStartMs ?: return
        val endMs = System.currentTimeMillis().coerceAtLeast(startMs + 1_000L)
        val disturbances = disturbanceCount

        microphoneRecorder.stop()
        recordingJob?.cancel()
        levelJob?.cancel()
        recordingJob = null
        levelJob = null
        recordingStartMs = null
        disturbanceCount = 0
        lastDisturbanceMs = 0L
        _liveDbfs.value = -60f
        _uiState.update { it.copy(liveSnoreDb = null) }

        viewModelScope.launch {
            sleepRecordDao.insert(
                SleepRecordEntity(
                    startTimeMs = startMs,
                    endTimeMs = endMs,
                    disturbanceCount = disturbances,
                    sleepScore = calculateScore(endMs - startMs, disturbances)
                )
            )
        }
    }

    private fun calculateScore(durationMs: Long, disturbanceCount: Int): Int {
        val durationHours = durationMs / 3_600_000.0
        val base = (durationHours / 8.0 * 80.0).coerceIn(0.0, 80.0)
        val penalty = disturbanceCount * 4
        val bonus = (0..10).random()
        return (base - penalty + bonus).toInt().coerceIn(0, 100)
    }

    override fun onCleared() {
        microphoneRecorder.stop()
        super.onCleared()
    }

    private companion object {
        const val RECORDING_THRESHOLD_DBFS = -20f
        const val DISTURBANCE_COOLDOWN_MS = 5_000L
    }
}