# Kiro File Manager — ProGuard/R8 rules

# Keep Hilt generated components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**

# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Shizuku is optional at runtime — do not fail if classes are absent.
-dontwarn rikka.shizuku.**
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }

# Media3
-dontwarn androidx.media3.**

# Keep data models used via reflection by serialization / Room
-keepclassmembers class com.kiro.filemanager.data.local.entity.** { *; }

# Compose
-dontwarn org.jetbrains.annotations.**
