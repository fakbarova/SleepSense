package com.circadianx.sleepsense.data.network

import com.circadianx.sleepsense.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

private data class VerifyRequest(val idToken: String)
data class VerifyResponse(
    val ok: Boolean,
    val userId: String?,
    val email: String?,
    val name: String?
)

@Singleton
class AuthApiRepository @Inject constructor(
    private val gson: Gson,
    private val httpClient: OkHttpClient
) {
    private val json = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = BuildConfig.BACKEND_URL

    suspend fun verifyToken(idToken: String): Result<VerifyResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val requestBody = gson.toJson(VerifyRequest(idToken)).toRequestBody(json)
            val request = Request.Builder()
                .url("$baseUrl/auth/verify")
                .post(requestBody)
                .build()
            httpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    error("Token verification failed: HTTP ${response.code} $raw")
                }
                gson.fromJson(raw, VerifyResponse::class.java)
            }
        }
    }
}
