package com.circadianx.sleepsense.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "memberName"],
    indices = [Index(value = ["groupId"])]
)
data class GroupMemberEntity(
    val groupId: Long,
    val memberName: String,
    val joinedAtMs: Long
)

