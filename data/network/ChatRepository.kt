package com.circadianx.sleepsense.data.network

import com.circadianx.sleepsense.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// ── Request / response shapes ────────────────────────────────────────────────

data class ChatSleepContext(
    val recentScores: List<Int> = emptyList(),
    val recentDurationsMinutes: List<Long> = emptyList(),
    val recentDisturbances: List<Int> = emptyList()
)

data class ChatHabitsContext(
    val completionRate7d: Float? = null,
    val mostMissed: String? = null
)

data class ChatStepsContext(
    val recentSteps: List<Int> = emptyList()
)

data class ChatContext(
    val sleep: ChatSleepContext? = null,
    val habits: ChatHabitsContext? = null,
    val steps: ChatStepsContext? = null,
    val goals: List<String> = emptyList()
)

data class ChatRequest(
    val question: String,
    val context: ChatContext
)

data class ChatResponse(
    val answer: String
)

// ── Repository ────────────────────────────────────────────────────────────────

@Singleton
class ChatRepository @Inject constructor(
    private val gson: Gson,
    private val httpClient: OkHttpClient
) {
    private val json = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = BuildConfig.BACKEND_URL

    suspend fun sendMessage(
        question: String,
        context: ChatContext
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(ChatRequest(question = question, context = context))
                .toRequestBody(json)

            val request = Request.Builder()
                .url("$baseUrl/chat")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val raw = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Result.failure(Exception("Server error ${response.code}: $raw"))
            } else {
                val parsed = gson.fromJson(raw, ChatResponse::class.java)
                Result.success(parsed.answer)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Health-check — returns true if the backend is reachable. */
    suspend fun isReachable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$baseUrl/health").get().build()
            httpClient.newCall(request).execute().isSuccessful
        } catch (_: Exception) {
            false
        }
    }
}
