package com.circadianx.sleepsense.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.circadianx.sleepsense.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val idToken: StateFlow<String?> = dataStore.data
        .map { prefs -> prefs[UserPreferences.AUTH_ID_TOKEN]?.takeIf { it.isNotBlank() } }
        .stateIn(scope, SharingStarted.Eagerly, null)

    val email: StateFlow<String?> = dataStore.data
        .map { prefs -> prefs[UserPreferences.AUTH_EMAIL]?.takeIf { it.isNotBlank() } }
        .stateIn(scope, SharingStarted.Eagerly, null)

    val displayName: StateFlow<String?> = dataStore.data
        .map { prefs -> prefs[UserPreferences.NAME]?.takeIf { it.isNotBlank() } }
        .stateIn(scope, SharingStarted.Eagerly, null)

    fun currentToken(): String? = idToken.value

    fun currentEmail(): String? = email.value

    fun persistAuth(idToken: String, email: String?, name: String?) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[UserPreferences.AUTH_ID_TOKEN] = idToken
                if (!email.isNullOrBlank()) {
                    prefs[UserPreferences.AUTH_EMAIL] = email
                } else {
                    prefs.remove(UserPreferences.AUTH_EMAIL)
                }
                if (!name.isNullOrBlank()) {
                    prefs[UserPreferences.NAME] = name
                }
            }
        }
    }

    fun signOut() {
        scope.launch {
            dataStore.edit { prefs ->
                prefs.remove(UserPreferences.AUTH_ID_TOKEN)
                prefs.remove(UserPreferences.AUTH_EMAIL)
            }
        }
    }
}
