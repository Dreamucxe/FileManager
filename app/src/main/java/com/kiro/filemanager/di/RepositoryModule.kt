package com.kiro.filemanager.di

import com.kiro.filemanager.data.repository.ApkRepositoryImpl
import com.kiro.filemanager.data.repository.ArchiveRepositoryImpl
import com.kiro.filemanager.data.repository.BookmarkRepositoryImpl
import com.kiro.filemanager.data.repository.FileRepositoryImpl
import com.kiro.filemanager.data.repository.MetadataRepositoryImpl
import com.kiro.filemanager.data.repository.RecycleBinRepositoryImpl
import com.kiro.filemanager.data.settings.SettingsRepositoryImpl
import com.kiro.filemanager.domain.repository.ApkRepository
import com.kiro.filemanager.domain.repository.ArchiveRepository
import com.kiro.filemanager.domain.repository.BookmarkRepository
import com.kiro.filemanager.domain.repository.FileRepository
import com.kiro.filemanager.domain.repository.MetadataRepository
import com.kiro.filemanager.domain.repository.RecycleBinRepository
import com.kiro.filemanager.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindRecycleBinRepository(impl: RecycleBinRepositoryImpl): RecycleBinRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindArchiveRepository(impl: ArchiveRepositoryImpl): ArchiveRepository

    @Binds
    @Singleton
    abstract fun bindApkRepository(impl: ApkRepositoryImpl): ApkRepository

    @Binds
    @Singleton
    abstract fun bindMetadataRepository(impl: MetadataRepositoryImpl): MetadataRepository
}
