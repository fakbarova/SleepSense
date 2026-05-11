package com.circadianx.sleepsense.data.spotify

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.circadianx.sleepsense.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyAuthRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun prepareAuthorizeUrl(
        spotifyClientId: String,
        redirectUri: String,
        scopes: List<String> = listOf("user-read-private")
    ): String {
        val verifier = SpotifyPkce.createVerifier()
        val challenge = SpotifyPkce.createChallenge(verifier)

        dataStore.edit { prefs ->
            prefs[UserPreferences.SPOTIFY_CODE_VERIFIER] = verifier
        }

        val uri = Uri.Builder()
            .scheme("https")
            .authority("accounts.spotify.com")
            .appendPath("authorize")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", spotifyClientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("scope", scopes.joinToString(" "))
            .build()

        return uri.toString()
    }

    suspend fun popVerifier(): String? {
        val verifier = dataStore.data.first()[UserPreferences.SPOTIFY_CODE_VERIFIER]
        dataStore.edit { prefs -> prefs.remove(UserPreferences.SPOTIFY_CODE_VERIFIER) }
        return verifier
    }
}

