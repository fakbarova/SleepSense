package com.circadianx.sleepsense.domain.model

data class SleepQualityScore(
    val value: Int,
    val factors: List<Factor>
) {
    init {
        require(value in 0..100) { "SleepQualityScore.value must be 0..100" }
    }

    data class Factor(
        val label: String,
        val contribution: Int
    )
}

