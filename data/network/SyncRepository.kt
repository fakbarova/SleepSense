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

data class SyncedChallenge(
    val id: String,
    val title: String,
    val category: String,
    val durationDays: Int,
    val successCriteria: String,
    val startEpochDay: Long,
    val createdAtMs: Long,
    val completedToday: Boolean
)

data class SyncedHabit(
    val id: String,
    val type: String,
    val title: String,
    val reminderMinutesOfDay: Int?,
    val enabled: Boolean,
    val completedToday: Boolean
)

private data class ChallengesResponse(val challenges: List<SyncedChallenge> = emptyList())
private data class ChallengesRequest(val challenges: List<SyncedChallenge>)
private data class HabitsResponse(val habits: List<SyncedHabit> = emptyList())
private data class HabitsRequest(val habits: List<SyncedHabit>)

@Singleton
class SyncRepository @Inject constructor(
    private val gson: Gson,
    private val httpClient: OkHttpClient
) {
    private val json = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = BuildConfig.BACKEND_URL

    suspend fun fetchChallenges(): Result<List<SyncedChallenge>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/sync/challenges")
                .get()
                .build()
            httpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    error("fetchChallenges failed: ${response.code} $raw")
                }
                gson.fromJson(raw, ChallengesResponse::class.java).challenges
            }
        }
    }

    suspend fun pushChallenges(challenges: List<SyncedChallenge>): Result<List<SyncedChallenge>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = gson.toJson(ChallengesRequest(challenges)).toRequestBody(json)
                val request = Request.Builder()
                    .url("$baseUrl/sync/challenges")
                    .put(body)
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        error("pushChallenges failed: ${response.code} $raw")
                    }
                    gson.fromJson(raw, ChallengesResponse::class.java).challenges
                }
            }
        }

    suspend fun fetchHabits(): Result<List<SyncedHabit>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/sync/habits")
                .get()
                .build()
            httpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    error("fetchHabits failed: ${response.code} $raw")
                }
                gson.fromJson(raw, HabitsResponse::class.java).habits
            }
        }
    }

    suspend fun pushHabits(habits: List<SyncedHabit>): Result<List<SyncedHabit>> = withContext(Dispatchers.IO) {
        runCatching {
            val body = gson.toJson(HabitsRequest(habits)).toRequestBody(json)
            val request = Request.Builder()
                .url("$baseUrl/sync/habits")
                .put(body)
                .build()
            httpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    error("pushHabits failed: ${response.code} $raw")
                }
                gson.fromJson(raw, HabitsResponse::class.java).habits
            }
        }
    }
}
