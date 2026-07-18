package com.kiro.filemanager.core.util

import javax.inject.Qualifier

/** Qualifier for the IO dispatcher, so tests can swap in a test dispatcher. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/** Application-scoped CoroutineScope qualifier. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
