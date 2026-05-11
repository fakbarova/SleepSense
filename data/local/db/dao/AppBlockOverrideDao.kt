package com.circadianx.sleepsense.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.circadianx.sleepsense.data.local.db.entity.AppBlockOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppBlockOverrideDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AppBlockOverrideEntity): Long

    @Query("SELECT * FROM app_block_overrides ORDER BY timestampMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<AppBlockOverrideEntity>>
}

