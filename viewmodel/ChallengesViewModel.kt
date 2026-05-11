package com.circadianx.sleepsense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.data.local.db.dao.ChallengeDao
import com.circadianx.sleepsense.data.local.db.entity.ChallengeCheckInEntity
import com.circadianx.sleepsense.data.local.db.entity.ChallengeEntity
import com.circadianx.sleepsense.data.network.SyncRepository
import com.circadianx.sleepsense.data.network.SyncedChallenge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ChallengesUiState(
    val challenges: List<ChallengeEntity> = emptyList()
)

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val syncRepository: SyncRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    private val todayEpochDay: Long get() = LocalDate.now().toEpochDay()

    init {
        viewModelScope.launch {
            challengeDao.observeChallenges().collectLatest { list ->
                _uiState.update { it.copy(challenges = list) }
            }
        }
        viewModelScope.launch { syncFromCloud() }
    }

    fun createChallenge(
        title: String,
        category: String,
        durationDays: Int,
        successCriteria: String
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return
        viewModelScope.launch {
            challengeDao.insertChallenge(
                ChallengeEntity(
                    title = trimmedTitle,
                    category = category,
                    durationDays = durationDays.coerceIn(7, 90),
                    successCriteria = successCriteria.trim().ifEmpty { "Complete daily" },
                    startEpochDay = todayEpochDay,
                    createdAtMs = System.currentTimeMillis()
                )
            )
            syncToCloud()
        }
    }

    fun toggleTodayComplete(challengeId: Long, currentlyCompleted: Boolean) {
        viewModelScope.launch {
            challengeDao.upsertCheckIn(
                ChallengeCheckInEntity(
                    challengeId = challengeId,
                    epochDay = todayEpochDay,
                    completed = !currentlyCompleted,
                    updatedAtMs = System.currentTimeMillis()
                )
            )
            syncToCloud()
        }
    }

    private suspend fun syncFromCloud() {
        val today = todayEpochDay
        val result = syncRepository.fetchChallenges()
        val remote = result.getOrNull() ?: return
        challengeDao.deleteAllCheckIns()
        challengeDao.deleteAllChallenges()
        challengeDao.insertChallenges(remote.map { item ->
            ChallengeEntity(
                id = item.id.toLongOrNull() ?: item.id.hashCode().toLong(),
                title = item.title,
                category = item.category,
                durationDays = item.durationDays,
                successCriteria = item.successCriteria,
                startEpochDay = item.startEpochDay,
                createdAtMs = item.createdAtMs
            )
        })
        remote.forEach { item ->
            challengeDao.upsertCheckIn(
                ChallengeCheckInEntity(
                    challengeId = item.id.toLongOrNull() ?: item.id.hashCode().toLong(),
                    epochDay = today,
                    completed = item.completedToday,
                    updatedAtMs = System.currentTimeMillis()
                )
            )
        }
    }

    private suspend fun syncToCloud() {
        val today = todayEpochDay
        val challenges = challengeDao.getAllChallenges()
        val checkinsById = challengeDao.getCheckInsForDay(today).associateBy { it.challengeId }
        val payload = challenges.map { challenge ->
            SyncedChallenge(
                id = challenge.id.toString(),
                title = challenge.title,
                category = challenge.category,
                durationDays = challenge.durationDays,
                successCriteria = challenge.successCriteria,
                startEpochDay = challenge.startEpochDay,
                createdAtMs = challenge.createdAtMs,
                completedToday = checkinsById[challenge.id]?.completed ?: false
            )
        }
        syncRepository.pushChallenges(payload)
    }
}

