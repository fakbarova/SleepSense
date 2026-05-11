package com.circadianx.sleepsense.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_block_overrides",
    indices = [Index(value = ["timestampMs"])]
)
data class AppBlockOverrideEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestampMs: Long,
    val packageName: String,
    val reason: String
)

