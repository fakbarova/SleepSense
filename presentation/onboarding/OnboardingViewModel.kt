package com.circadianx.sleepsense.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.domain.repository.BedtimeSchedule
import com.circadianx.sleepsense.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val onboardingCompleted: Boolean,
    val bedtimeSchedule: BedtimeSchedule?,
    val primaryGoals: Set<String>
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository
) : ViewModel() {
    val state: StateFlow<OnboardingState> =
        combine(
            prefs.onboardingCompleted,
            prefs.bedtimeSchedule,
            prefs.primaryGoals
        ) { completed, schedule, goals ->
            OnboardingState(completed, schedule, goals)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            OnboardingState(onboardingCompleted = false, bedtimeSchedule = null, primaryGoals = emptySet())
        )

    fun setSchedule(targetBedtimeMinutes: Int, targetWakeMinutes: Int) {
        viewModelScope.launch {
            prefs.setBedtimeSchedule(BedtimeSchedule(targetBedtimeMinutes, targetWakeMinutes))
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            prefs.setOnboardingCompleted(true)
        }
    }

    fun setGoals(goals: Set<String>) {
        viewModelScope.launch {
            prefs.setPrimaryGoals(goals)
        }
    }
}

