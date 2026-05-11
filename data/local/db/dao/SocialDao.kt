package com.circadianx.sleepsense.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.circadianx.sleepsense.data.local.db.entity.GroupChallengeEntity
import com.circadianx.sleepsense.data.local.db.entity.GroupMemberEntity
import com.circadianx.sleepsense.data.local.db.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupChallengeEntity): Long

    @Query("SELECT * FROM group_challenges ORDER BY createdAtMs DESC")
    fun observeGroups(): Flow<List<GroupChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMemberEntity)

    @Query("SELECT * FROM group_members WHERE groupId = :groupId ORDER BY joinedAtMs ASC")
    fun observeMembers(groupId: Long): Flow<List<GroupMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity): Long

    @Query("SELECT * FROM stories ORDER BY createdAtMs DESC")
    fun observeStories(): Flow<List<StoryEntity>>
}

