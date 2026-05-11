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

data class LatLngDto(val lat: Double, val lng: Double)

data class RouteSuggestRequest(
    val origin: LatLngDto,
    val destination: LatLngDto,
    val stepsRemaining: Int
)

data class RouteSuggestion(
    val summary: String,
    val extraSteps: Int,
    val extraMinutes: Int,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val encodedPolyline: String
)

data class RouteSuggestResponse(val suggestion: RouteSuggestion)

@Singleton
class RouteRepository @Inject constructor(
    private val gson: Gson,
    private val httpClient: OkHttpClient
) {
    private val json = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = BuildConfig.BACKEND_URL

    suspend fun suggestRoute(
        origin: LatLngDto,
        destination: LatLngDto,
        stepsRemaining: Int
    ): Result<RouteSuggestion> = withContext(Dispatchers.IO) {
        runCatching {
            val body = gson.toJson(
                RouteSuggestRequest(
                    origin = origin,
                    destination = destination,
                    stepsRemaining = stepsRemaining
                )
            ).toRequestBody(json)

            val request = Request.Builder()
                .url("$baseUrl/routes/suggest")
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("routes/suggest failed: ${response.code} $raw")
                gson.fromJson(raw, RouteSuggestResponse::class.java).suggestion
            }
        }
    }
}

