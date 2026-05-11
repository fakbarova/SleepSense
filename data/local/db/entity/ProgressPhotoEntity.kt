package com.circadianx.sleepsense.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progress_photos",
    indices = [Index(value = ["challengeId"]), Index(value = ["takenAtMs"])]
)
data class ProgressPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val challengeId: Long?,
    val takenAtMs: Long,
    val epochDay: Long,
    val fileName: String,
    val ivBase64: String
)

