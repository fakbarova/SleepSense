package com.circadianx.sleepsense.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "challenges",
    indices = [Index(value = ["startEpochDay"])]
)
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // sleep | weight | screen_time | exercise | pain | custom
    val durationDays: Int,
    val successCriteria: String,
    val startEpochDay: Long,
    val createdAtMs: Long
)

