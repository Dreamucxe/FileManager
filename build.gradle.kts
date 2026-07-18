// AGP 8.5.2 bundles R8 8.5.35, whose enqueuer throws a ConcurrentModificationException
// in processDeferredAnnotations on this project. That bug spans the whole 8.5.x line;
// force R8 8.7.18 (where it's fixed) onto the buildscript classpath.
buildscript {
    repositories {
        maven { url = uri("https://storage.googleapis.com/r8-releases/raw") }
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools:r8:8.7.18")
    }
}

// Top-level build file. Plugins are declared here with `apply false` and applied per-module.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
