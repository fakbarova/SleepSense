package com.circadianx.sleepsense.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.data.local.db.dao.RoutineDao
import com.circadianx.sleepsense.data.local.db.entity.RoutineCompletionEntity
import com.circadianx.sleepsense.data.local.db.entity.RoutineItemEntity
import com.circadianx.sleepsense.data.network.SyncRepository
import com.circadianx.sleepsense.data.network.SyncedHabit
import com.circadianx.sleepsense.util.RoutineAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitsUiState(
    val preSleep: List<RoutineItemUi> = emptyList(),
    val morning: List<RoutineItemUi> = emptyList()
)

data class RoutineItemUi(
    val id: Long,
    val type: String,
    val title: String,
    val reminderMinutesOfDay: Int?,
    val enabled: Boolean,
    val completedToday: Boolean
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModel @Inject constructor(
    private val routineDao: RoutineDao,
    private val syncRepository: SyncRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    private val currentEpochDay = MutableStateFlow(LocalDate.now().toEpochDay())

    init {
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                val newDay = LocalDate.now().toEpochDay()
                if (newDay != currentEpochDay.value) {
                    currentEpochDay.value = newDay
                }
            }
        }
        viewModelScope.launch {
            ensureDefaults()
            syncFromCloud()
            combine(
                routineDao.observeAllItems(),
                currentEpochDay.flatMapLatest { day -> routineDao.observeCompletionsForDay(day) }
            ) { items, completions ->
                val completedSet = completions.map { it.itemId }.toSet()
                items.map { item ->
                    RoutineItemUi(
                        id = item.id,
                        type = item.type,
                        title = item.title,
                        reminderMinutesOfDay = item.reminderMinutesOfDay,
                        enabled = item.enabled,
                        completedToday = completedSet.contains(item.id)
                    )
                }
            }.collectLatest { itemsUi ->
                _uiState.update {
                    it.copy(
                        preSleep = itemsUi.filter { x -> x.type == TYPE_PRE_SLEEP },
                        morning = itemsUi.filter { x -> x.type == TYPE_MORNING }
                    )
                }

                // Best-effort scheduling (idempotent enough for MVP)
                itemsUi.forEach { item ->
                    val mins = item.reminderMinutesOfDay
                    if (item.enabled && mins != null) {
                        RoutineAlarmScheduler.scheduleReminder(context, item.id, item.title, mins)
                    } else {
                        RoutineAlarmScheduler.cancelReminder(context, item.id)
                    }
                }
            }
        }
    }

    fun toggleCompleted(item: RoutineItemUi) {
        viewModelScope.launch {
            val epochDay = currentEpochDay.value
            val existing = routineDao.getCompletion(epochDay, item.id)
            if (existing == null) {
                routineDao.insertCompletion(
                    RoutineCompletionEntity(
                        itemId = item.id,
                        epochDay = epochDay,
                        completedAtMs = System.currentTimeMillis()
                    )
                )
            } else {
                routineDao.deleteCompletion(epochDay, item.id)
            }
            syncToCloud()
        }
    }

    private suspend fun ensureDefaults() {
        if (routineDao.countItems() > 0) return
        routineDao.insertItems(
            listOf(
                RoutineItemEntity(type = TYPE_PRE_SLEEP, title = "Brush teeth", reminderMinutesOfDay = 22 * 60 + 0),
                RoutineItemEntity(type = TYPE_PRE_SLEEP, title = "Drink water", reminderMinutesOfDay = 22 * 60 + 10),
                RoutineItemEntity(type = TYPE_PRE_SLEEP, title = "Light stretching (3–5 min)", reminderMinutesOfDay = 22 * 60 + 30),
                RoutineItemEntity(type = TYPE_MORNING, title = "Hydration", reminderMinutesOfDay = 7 * 60 + 10),
                RoutineItemEntity(type = TYPE_MORNING, title = "Short walk (10 min)", reminderMinutesOfDay = 7 * 60 + 40)
            )
        )
    }

    private suspend fun syncFromCloud() {
        val today = currentEpochDay.value
        val remote = syncRepository.fetchHabits().getOrNull() ?: return
        if (remote.isEmpty()) return
        routineDao.deleteAllCompletions()
        routineDao.deleteAllItems()
        routineDao.insertItems(
            remote.map { habit ->
                RoutineItemEntity(
                    id = habit.id.toLongOrNull() ?: habit.id.hashCode().toLong(),
                    type = habit.type,
                    title = habit.title,
                    reminderMinutesOfDay = habit.reminderMinutesOfDay,
                    enabled = habit.enabled
                )
            }
        )
        routineDao.insertCompletions(
            remote.filter { it.completedToday }.map { habit ->
                RoutineCompletionEntity(
                    itemId = habit.id.toLongOrNull() ?: habit.id.hashCode().toLong(),
                    epochDay = today,
                    completedAtMs = System.currentTimeMillis()
                )
            }
        )
    }

    private suspend fun syncToCloud() {
        val today = currentEpochDay.value
        val completions = routineDao.getCompletionsForDay(today).associateBy { it.itemId }
        val habits = routineDao.getAllItems().map { item ->
            SyncedHabit(
                id = item.id.toString(),
                type = item.type,
                title = item.title,
                reminderMinutesOfDay = item.reminderMinutesOfDay,
                enabled = item.enabled,
                completedToday = completions[item.id] != null
            )
        }
        syncRepository.pushHabits(habits)
    }

    companion object {
        const val TYPE_PRE_SLEEP = "pre_sleep"
        const val TYPE_MORNING = "morning"
    }
}

