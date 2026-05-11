package com.circadianx.sleepsense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.circadianx.sleepsense.data.preferences.UserPreferences
import com.circadianx.sleepsense.data.network.SpotifyRepository
import com.circadianx.sleepsense.data.spotify.SpotifyAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpotifyUiState(
    val isLoading: Boolean = false,
    val authorizeUrl: String? = null,
    val connected: Boolean = false,
    val windDownLink: String? = null,
    val error: String? = null
)

@HiltViewModel
class SpotifyViewModel @Inject constructor(
    private val spotifyAuthRepository: SpotifyAuthRepository,
    private val spotifyRepository: SpotifyRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _state = MutableStateFlow(SpotifyUiState())
    val state: StateFlow<SpotifyUiState> = _state.asStateFlow()

    fun buildAuthorizeUrl(clientId: String, redirectUri: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                spotifyAuthRepository.prepareAuthorizeUrl(clientId, redirectUri)
            }.onSuccess { url ->
                _state.update { it.copy(isLoading = false, authorizeUrl = url) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Spotify auth failed") }
            }
        }
    }

    fun handleAuthCode(code: String, redirectUri: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val verifier = spotifyAuthRepository.popVerifier()
            if (verifier.isNullOrBlank()) {
                _state.update { it.copy(isLoading = false, error = "Missing PKCE verifier") }
                return@launch
            }
            val result = spotifyRepository.exchangeCode(code, verifier, redirectUri)
            result.fold(
                onSuccess = { _state.update { it.copy(isLoading = false, connected = true) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Spotify connect failed") } }
            )
        }
    }

    fun consumePendingAuthCode(redirectUri: String) {
        viewModelScope.launch {
            val code = dataStore.data.first()[UserPreferences.SPOTIFY_AUTH_CODE]
            if (code.isNullOrBlank()) return@launch
            dataStore.edit { it.remove(UserPreferences.SPOTIFY_AUTH_CODE) }
            handleAuthCode(code, redirectUri)
        }
    }

    fun loadWindDownLink() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = spotifyRepository.windDownLink()
            result.fold(
                onSuccess = { link -> _state.update { it.copy(isLoading = false, windDownLink = link) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Unable to load playlist") } }
            )
        }
    }
}

