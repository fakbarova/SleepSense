package com.circadianx.sleepsense.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_completions",
    indices = [Index(value = ["epochDay", "itemId"], unique = true)]
)
data class RoutineCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val epochDay: Long,
    val completedAtMs: Long
)

