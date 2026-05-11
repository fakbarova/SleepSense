package com.circadianx.sleepsense.spotify

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.circadianx.sleepsense.data.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SpotifyCallbackActivity : ComponentActivity() {
    @Inject lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri: Uri? = intent?.data
        val code = uri?.getQueryParameter("code")
        if (!code.isNullOrBlank()) {
            // Best-effort handoff to the app via DataStore.
            // The main UI can observe and consume this once.
            lifecycleScope.launchWhenStarted {
                dataStore.edit { prefs ->
                    prefs[UserPreferences.SPOTIFY_AUTH_CODE] = code
                }
                finish()
            }
        } else {
            finish()
        }
    }
}

