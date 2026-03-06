# CallFilter ProGuard Rules

# Keep Room entities and DAOs
-keep class fr.lachemoilagrappe.data.local.db.entity.** { *; }
-keep class fr.lachemoilagrappe.data.local.db.dao.** { *; }
-keep class fr.lachemoilagrappe.data.local.db.Converters { *; }

# Keep Room database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep libphonenumber metadata
-keep class com.google.i18n.phonenumbers.** { *; }
-keepclassmembers class com.google.i18n.phonenumbers.** { *; }

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Keep enum classes used by Room converters
-keepclassmembers enum fr.lachemoilagrappe.domain.model.** { *; }

# Keep CallScreeningService
-keep class fr.lachemoilagrappe.service.CallFilterScreeningService { *; }
-keep class fr.lachemoilagrappe.service.ActionReceiver { *; }

# Keep WorkManager workers
-keep class fr.lachemoilagrappe.worker.** { *; }

# SQLCipher
-keep class net.zetetic.database.** { *; }

# Keep DataStore
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
-assumenosideeffects class timber.log.Timber {
    public static void v(...);
    public static void d(...);
    public static void i(...);
}
