package com.circadianx.sleepsense.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.circadianx.sleepsense.data.local.db.entity.StepDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: StepDayEntity)

    @Query("SELECT * FROM step_days ORDER BY epochDay DESC LIMIT :limit")
    fun observeRecent(limit: Int = 14): Flow<List<StepDayEntity>>

    @Query("SELECT * FROM step_days WHERE epochDay = :epochDay LIMIT 1")
    suspend fun get(epochDay: Long): StepDayEntity?
}

