package com.circadianx.sleepsense.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_days")
data class StepDayEntity(
    @PrimaryKey
    val epochDay: Long,
    val steps: Int,
    val updatedAtMs: Long
)

