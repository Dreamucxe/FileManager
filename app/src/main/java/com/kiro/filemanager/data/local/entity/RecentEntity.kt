package com.kiro.filemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentEntity(
    @PrimaryKey val path: String,
    val name: String,
    val isDirectory: Boolean,
    val accessedAt: Long,
)
