package com.circadianx.sleepsense.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "night_disturbances",
    indices = [Index(value = ["timestampMs"])]
)
data class NightDisturbanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestampMs: Long,
    val durationMs: Long
)

