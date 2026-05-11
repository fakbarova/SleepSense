package com.circadianx.sleepsense.domain.repository

import kotlinx.coroutines.flow.Flow

data class BedtimeSchedule(
    val targetBedtimeMinutes: Int,
    val targetWakeMinutes: Int
)

interface UserPreferencesRepository {
    val bedtimeSchedule: Flow<BedtimeSchedule?>
    val onboardingCompleted: Flow<Boolean>
    val primaryGoals: Flow<Set<String>>

    suspend fun setBedtimeSchedule(schedule: BedtimeSchedule)
    suspend fun setPrimaryGoals(goals: Set<String>)
    suspend fun setOnboardingCompleted(completed: Boolean)
}

