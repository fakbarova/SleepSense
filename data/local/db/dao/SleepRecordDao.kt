package com.circadianx.sleepsense.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.circadianx.sleepsense.data.local.db.entity.SleepRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SleepRecordEntity): Long

    @Query("SELECT * FROM sleep_records ORDER BY startTimeMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 14): Flow<List<SleepRecordEntity>>
}

