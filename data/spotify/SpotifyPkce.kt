package com.circadianx.sleepsense.data.spotify

import android.util.Base64
import java.security.MessageDigest
import kotlin.random.Random

object SpotifyPkce {
    fun createVerifier(): String {
        val bytes = ByteArray(48)
        Random.Default.nextBytes(bytes)
        return base64Url(bytes)
    }

    fun createChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashed = digest.digest(verifier.toByteArray(Charsets.US_ASCII))
        return base64Url(hashed)
    }

    private fun base64Url(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
}

