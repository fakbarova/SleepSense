package com.circadianx.sleepsense.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.circadianx.sleepsense.data.local.db.entity.RoutineCompletionEntity
import com.circadianx.sleepsense.data.local.db.entity.RoutineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routine_items ORDER BY type ASC, id ASC")
    fun observeAllItems(): Flow<List<RoutineItemEntity>>

    @Query("SELECT COUNT(*) FROM routine_items")
    suspend fun countItems(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<RoutineItemEntity>)

    @Query("DELETE FROM routine_items")
    suspend fun deleteAllItems()

    @Query("SELECT * FROM routine_completions WHERE epochDay = :epochDay")
    fun observeCompletionsForDay(epochDay: Long): Flow<List<RoutineCompletionEntity>>

    @Query("SELECT * FROM routine_items ORDER BY type ASC, id ASC")
    suspend fun getAllItems(): List<RoutineItemEntity>

    @Query("SELECT * FROM routine_completions WHERE epochDay = :epochDay")
    suspend fun getCompletionsForDay(epochDay: Long): List<RoutineCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<RoutineCompletionEntity>)

    @Query("DELETE FROM routine_completions")
    suspend fun deleteAllCompletions()

    @Query("SELECT * FROM routine_completions WHERE epochDay = :epochDay AND itemId = :itemId LIMIT 1")
    suspend fun getCompletion(epochDay: Long, itemId: Long): RoutineCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: RoutineCompletionEntity): Long

    @Query("DELETE FROM routine_completions WHERE epochDay = :epochDay AND itemId = :itemId")
    suspend fun deleteCompletion(epochDay: Long, itemId: Long)
}

