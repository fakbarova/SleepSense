package com.circadianx.sleepsense.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_items",
    indices = [Index(value = ["type"])]
)
data class RoutineItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "pre_sleep" | "morning"
    val title: String,
    val reminderMinutesOfDay: Int?,
    val enabled: Boolean = true
)

