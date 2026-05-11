package com.circadianx.sleepsense.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.circadianx.sleepsense.data.local.db.entity.NightDisturbanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NightDisturbanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: NightDisturbanceEntity): Long

    @Query("SELECT * FROM night_disturbances WHERE timestampMs BETWEEN :startMs AND :endMs ORDER BY timestampMs ASC")
    fun observeInRange(startMs: Long, endMs: Long): Flow<List<NightDisturbanceEntity>>

    @Query("SELECT COUNT(*) FROM night_disturbances WHERE timestampMs BETWEEN :startMs AND :endMs")
    suspend fun countInRange(startMs: Long, endMs: Long): Int
}

