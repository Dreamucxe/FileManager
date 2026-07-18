package com.kiro.filemanager.di

import android.content.Context
import androidx.room.Room
import com.kiro.filemanager.data.local.dao.FavoriteDao
import com.kiro.filemanager.data.local.dao.RecentDao
import com.kiro.filemanager.data.local.dao.TrashDao
import com.kiro.filemanager.data.local.db.KiroDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KiroDatabase =
        Room.databaseBuilder(context, KiroDatabase::class.java, KiroDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFavoriteDao(db: KiroDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideRecentDao(db: KiroDatabase): RecentDao = db.recentDao()

    @Provides
    fun provideTrashDao(db: KiroDatabase): TrashDao = db.trashDao()
}
