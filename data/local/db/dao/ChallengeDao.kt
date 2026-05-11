package com.circadianx.sleepsense.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.circadianx.sleepsense.data.local.db.entity.ChallengeCheckInEntity
import com.circadianx.sleepsense.data.local.db.entity.ChallengeEntity
import com.circadianx.sleepsense.data.local.db.entity.ChallengeRatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: ChallengeEntity): Long

    @Query("SELECT * FROM challenges ORDER BY createdAtMs DESC")
    fun observeChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges ORDER BY createdAtMs DESC")
    suspend fun getAllChallenges(): List<ChallengeEntity>

    @Query("SELECT * FROM challenges WHERE id = :id LIMIT 1")
    suspend fun getChallenge(id: Long): ChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Query("DELETE FROM challenges")
    suspend fun deleteAllChallenges()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIn(checkIn: ChallengeCheckInEntity)

    @Query("SELECT * FROM challenge_checkins WHERE challengeId = :challengeId ORDER BY epochDay ASC")
    fun observeCheckIns(challengeId: Long): Flow<List<ChallengeCheckInEntity>>

    @Query("SELECT * FROM challenge_checkins WHERE epochDay = :epochDay")
    suspend fun getCheckInsForDay(epochDay: Long): List<ChallengeCheckInEntity>

    @Query("DELETE FROM challenge_checkins")
    suspend fun deleteAllCheckIns()

    @Query("SELECT COUNT(*) FROM challenge_checkins WHERE challengeId = :challengeId AND completed = 1")
    suspend fun countCompletedDays(challengeId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRating(rating: ChallengeRatingEntity)

    @Query("SELECT * FROM challenge_ratings WHERE challengeId = :challengeId LIMIT 1")
    suspend fun getRating(challengeId: Long): ChallengeRatingEntity?
}

