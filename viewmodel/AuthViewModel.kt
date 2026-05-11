package com.circadianx.sleepsense.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.data.auth.AuthSessionManager
import com.circadianx.sleepsense.data.auth.GoogleAuthRepository
import com.circadianx.sleepsense.data.network.AuthApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val email: String? = null,
    val name: String? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val googleAuthRepository: GoogleAuthRepository,
    private val authApiRepository: AuthApiRepository,
    private val authSessionManager: AuthSessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AuthUiState(
            isSignedIn = authSessionManager.currentToken() != null,
            email = authSessionManager.currentEmail(),
            name = authSessionManager.displayName.value
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val googleResult = googleAuthRepository.signIn(activity)
            val identity = googleResult.getOrElse { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message ?: "Google sign-in failed")
                }
                return@launch
            }

            val verifyResult = authApiRepository.verifyToken(identity.idToken)
            verifyResult.fold(
                onSuccess = { response ->
                    if (!response.ok) {
                        _uiState.update {
                            it.copy(isLoading = false, error = "Token verification failed")
                        }
                        return@fold
                    }
                    authSessionManager.persistAuth(
                        idToken = identity.idToken,
                        email = response.email ?: identity.email,
                        name = response.name ?: identity.name
                    )
                    _uiState.update {
                        it.copy(
                            isSignedIn = true,
                            isLoading = false,
                            email = response.email ?: identity.email,
                            name = response.name ?: identity.name,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSignedIn = false,
                            isLoading = false,
                            error = error.message ?: "Unable to verify sign-in"
                        )
                    }
                }
            )
        }
    }

    fun signOut() {
        authSessionManager.signOut()
        _uiState.update {
            it.copy(
                isSignedIn = false,
                email = null,
                name = null,
                error = null
            )
        }
    }
}
