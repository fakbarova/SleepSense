package com.circadianx.sleepsense.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "challenge_checkins",
    primaryKeys = ["challengeId", "epochDay"],
    indices = [Index(value = ["challengeId"]), Index(value = ["epochDay"])]
)
data class ChallengeCheckInEntity(
    val challengeId: Long,
    val epochDay: Long,
    val completed: Boolean,
    val updatedAtMs: Long
)

