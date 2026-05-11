package com.circadianx.sleepsense.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.circadianx.sleepsense.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class AuthIdentity(
    val idToken: String,
    val email: String?,
    val name: String?
)

@Singleton
class GoogleAuthRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(activity: Activity): Result<AuthIdentity> {
        val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (clientId.isBlank()) {
            return Result.failure(IllegalStateException("Missing GOOGLE_WEB_CLIENT_ID"))
        }

        return runCatching {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )
            val credential = result.credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                error("Unexpected credential type")
            }

            val googleIdTokenCredential = try {
                GoogleIdTokenCredential.createFrom(credential.data)
            } catch (e: GoogleIdTokenParsingException) {
                throw IllegalStateException("Unable to parse Google credential", e)
            }

            AuthIdentity(
                idToken = googleIdTokenCredential.idToken,
                email = googleIdTokenCredential.id,
                name = googleIdTokenCredential.displayName
            )
        }
    }
}
