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

data class ReportSleepRecord(
    val date: String,
    val score: Int,
    val durationMinutes: Long,
    val disturbances: Int
)

data class ReportRequest(
    val sleepRecords: List<ReportSleepRecord>,
    val steps: List<Int>,
    val goals: List<String>,
    val userName: String
)

data class NightHighlight(
    val date: String,
    val score: Int,
    val note: String
)

data class ReportHighlights(
    val bestNight: NightHighlight,
    val worstNight: NightHighlight
)

data class ReportResponse(
    val weeklyScore: Int,
    val previousWeekScore: Int,
    val trend: String,
    val patterns: List<String>,
    val riskAssessment: String,
    val recommendations: List<String>,
    val highlights: ReportHighlights
)

@Singleton
class ReportRepository @Inject constructor(
    private val gson: Gson,
    private val httpClient: OkHttpClient
) {
    private val json = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = BuildConfig.BACKEND_URL

    suspend fun generateReport(request: ReportRequest): Result<ReportResponse> = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(request).toRequestBody(json)
            val httpRequest = Request.Builder()
                .url("$baseUrl/report")
                .post(body)
                .build()

            val response = httpClient.newCall(httpRequest).execute()
            val raw = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Result.failure(Exception("Server error ${response.code}: $raw"))
            } else {
                Result.success(gson.fromJson(raw, ReportResponse::class.java))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
