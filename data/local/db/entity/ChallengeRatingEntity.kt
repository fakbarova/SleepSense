package com.circadianx.sleepsense.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenge_ratings")
data class ChallengeRatingEntity(
    @PrimaryKey
    val challengeId: Long,
    val thumbsUp: Boolean,
    val comment: String?,
    val ratedAtMs: Long
)

