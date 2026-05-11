package com.circadianx.sleepsense.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.circadianx.sleepsense.domain.repository.BedtimeSchedule
import com.circadianx.sleepsense.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {
    private object Keys {
        val TARGET_BEDTIME_MINUTES = intPreferencesKey("target_bedtime_minutes")
        val TARGET_WAKE_MINUTES = intPreferencesKey("target_wake_minutes")
        val ONBOARDING_COMPLETED = intPreferencesKey("onboarding_completed_int")
        val PRIMARY_GOALS = stringSetPreferencesKey("primary_goals")
    }

    override val bedtimeSchedule: Flow<BedtimeSchedule?> =
        dataStore.data.map { prefs ->
            val bed = prefs[Keys.TARGET_BEDTIME_MINUTES]
            val wake = prefs[Keys.TARGET_WAKE_MINUTES]
            if (bed == null || wake == null) null else BedtimeSchedule(bed, wake)
        }

    override val onboardingCompleted: Flow<Boolean> =
        dataStore.data.map { prefs -> (prefs[Keys.ONBOARDING_COMPLETED] ?: 0) == 1 }

    override val primaryGoals: Flow<Set<String>> =
        dataStore.data.map { prefs -> prefs[Keys.PRIMARY_GOALS] ?: emptySet() }

    override suspend fun setBedtimeSchedule(schedule: BedtimeSchedule) {
        dataStore.edit { prefs ->
            prefs[Keys.TARGET_BEDTIME_MINUTES] = schedule.targetBedtimeMinutes
            prefs[Keys.TARGET_WAKE_MINUTES] = schedule.targetWakeMinutes
        }
    }

    override suspend fun setPrimaryGoals(goals: Set<String>) {
        dataStore.edit { prefs ->
            prefs[Keys.PRIMARY_GOALS] = goals
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = if (completed) 1 else 0
        }
    }
}

