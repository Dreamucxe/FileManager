package com.kiro.filemanager.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kiro.filemanager.data.local.dao.FavoriteDao
import com.kiro.filemanager.data.local.dao.RecentDao
import com.kiro.filemanager.data.local.dao.TrashDao
import com.kiro.filemanager.data.local.entity.FavoriteEntity
import com.kiro.filemanager.data.local.entity.RecentEntity
import com.kiro.filemanager.data.local.entity.TrashEntity

@Database(
    entities = [FavoriteEntity::class, RecentEntity::class, TrashEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class KiroDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentDao(): RecentDao
    abstract fun trashDao(): TrashDao

    companion object {
        const val NAME = "kiro_files.db"
    }
}
