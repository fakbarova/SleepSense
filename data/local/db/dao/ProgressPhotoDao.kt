package com.circadianx.sleepsense.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.circadianx.sleepsense.data.local.db.entity.ProgressPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: ProgressPhotoEntity): Long

    @Query("SELECT * FROM progress_photos WHERE challengeId IS NULL OR challengeId = :challengeId ORDER BY takenAtMs ASC")
    fun observeForChallenge(challengeId: Long?): Flow<List<ProgressPhotoEntity>>

    @Query("SELECT * FROM progress_photos ORDER BY takenAtMs ASC")
    fun observeAll(): Flow<List<ProgressPhotoEntity>>
}

