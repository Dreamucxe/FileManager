package com.kiro.filemanager

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point. Provides the Hilt-aware [HiltWorkerFactory] to
 * WorkManager (the default initializer is removed in the manifest so injected
 * workers resolve their dependencies).
 */
@HiltAndroidApp
class KiroApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
