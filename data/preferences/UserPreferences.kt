package com.circadianx.sleepsense.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object UserPreferences {
    val NAME                  = stringPreferencesKey("user_name")
    val AUTH_ID_TOKEN         = stringPreferencesKey("auth_id_token")
    val AUTH_EMAIL            = stringPreferencesKey("auth_email")
    val SPOTIFY_CODE_VERIFIER = stringPreferencesKey("spotify_code_verifier")
    val SPOTIFY_AUTH_CODE     = stringPreferencesKey("spotify_auth_code")
    val MIC_THRESHOLD_DBFS    = floatPreferencesKey("mic_threshold_dbfs")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
}
