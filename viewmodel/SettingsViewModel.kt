package com.circadianx.sleepsense.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.BuildConfig
import com.circadianx.sleepsense.data.auth.AuthSessionManager
import com.circadianx.sleepsense.data.preferences.UserPreferences
import com.circadianx.sleepsense.data.seed.DemoDataSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val demoDataSeeder: DemoDataSeeder,
    private val authSessionManager: AuthSessionManager
) : ViewModel() {

    private val _demoSeedStatus = MutableStateFlow<String?>(null)
    val demoSeedStatus: StateFlow<String?> = _demoSeedStatus

    val userName = dataStore.data
        .map { it[UserPreferences.NAME] ?: "User" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "User")

    val thresholdDbfs = dataStore.data
        .map { it[UserPreferences.MIC_THRESHOLD_DBFS] ?: -20f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), -20f)

    val notificationsEnabled = dataStore.data
        .map { it[UserPreferences.NOTIFICATIONS_ENABLED] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val authEmail = dataStore.data
        .map { it[UserPreferences.AUTH_EMAIL] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val signedIn = dataStore.data
        .map { !it[UserPreferences.AUTH_ID_TOKEN].isNullOrBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val canSeedDemoData = BuildConfig.DEBUG

    fun setName(name: String) = viewModelScope.launch {
        dataStore.edit { it[UserPreferences.NAME] = name }
    }

    fun setThreshold(value: Float) = viewModelScope.launch {
        dataStore.edit { it[UserPreferences.MIC_THRESHOLD_DBFS] = value }
    }

    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        dataStore.edit { it[UserPreferences.NOTIFICATIONS_ENABLED] = enabled }
    }

    fun seedDemoData() = viewModelScope.launch {
        if (!canSeedDemoData) return@launch
        _demoSeedStatus.value = "Loading demo data..."
        runCatching { demoDataSeeder.seed() }
            .onSuccess { _demoSeedStatus.value = "Demo data loaded" }
            .onFailure { _demoSeedStatus.value = "Demo data failed: ${it.message ?: "unknown error"}" }
    }

    fun signOut() {
        authSessionManager.signOut()
    }
}
