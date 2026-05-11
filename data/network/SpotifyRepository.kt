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

private data class SpotifyExchangeRequest(
    val code: String,
    val codeVerifier: String,
    val redirectUri: String
)

private data class WindDownResponse(
    val ok: Boolean,
    val uri: String?,
    val url: String?
)

@Singleton
class SpotifyRepository @Inject constructor(
    private val gson: Gson,
    private val httpClient: OkHttpClient
) {
    private val json = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = BuildConfig.BACKEND_URL

    suspend fun exchangeCode(code: String, verifier: String, redirectUri: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = gson.toJson(SpotifyExchangeRequest(code, verifier, redirectUri)).toRequestBody(json)
                val request = Request.Builder()
                    .url("$baseUrl/spotify/exchange")
                    .post(body)
                    .build()
                httpClient.newCall(request).execute().use { resp ->
                    val raw = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) error("spotify/exchange failed: ${resp.code} $raw")
                }
            }
        }

    suspend fun me(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/spotify/me")
                .get()
                .build()
            httpClient.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) error("spotify/me failed: ${resp.code} $raw")
                raw
            }
        }
    }

    suspend fun windDownLink(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/spotify/open/winddown")
                .get()
                .build()
            httpClient.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) error("spotify/open/winddown failed: ${resp.code} $raw")
                val parsed = gson.fromJson(raw, WindDownResponse::class.java)
                parsed.uri ?: parsed.url ?: error("No wind-down link returned")
            }
        }
    }
}

