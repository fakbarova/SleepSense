package com.circadianx.sleepsense.domain.usecase.sleep

import com.circadianx.sleepsense.domain.model.SleepQualityScore
import javax.inject.Inject

class CalculateSleepScoreUseCase @Inject constructor() {
    operator fun invoke(
        sleepDurationMinutes: Int,
        disturbanceCount: Int,
        bedtimeDeltaMinutes: Int,
        wakeDeltaMinutes: Int
    ): SleepQualityScore {
        // Placeholder implementation; real weighting will be added in Phase 1 routines.
        val durationScore = (sleepDurationMinutes / 6.0 * 50).toInt().coerceIn(0, 50)
        val disturbancePenalty = (disturbanceCount * 6).coerceIn(0, 30)
        val schedulePenalty = ((kotlin.math.abs(bedtimeDeltaMinutes) + kotlin.math.abs(wakeDeltaMinutes)) / 10)
            .coerceIn(0, 20)

        val value = (durationScore + (50 - disturbancePenalty - schedulePenalty)).coerceIn(0, 100)
        return SleepQualityScore(
            value = value,
            factors = listOf(
                SleepQualityScore.Factor("Duration", durationScore),
                SleepQualityScore.Factor("Disturbances", -disturbancePenalty),
                SleepQualityScore.Factor("Schedule mismatch", -schedulePenalty)
            )
        )
    }
}

