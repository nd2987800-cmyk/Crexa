# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep line numbers and source file attributes for Firebase Crashlytics deobfuscation
-keepattributes SourceFile,LineNumberTable,*Annotation*
-renamesourcefileattribute SourceFile

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Retrofit & OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-dontwarn okhttp3.**
-dontwarn okio.**

# Moshi & JSON Data Models
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
    @com.squareup.moshi.JsonClass <fields>;
}
-keep class com.example.data.gemini.** { *; }
-keep class com.example.data.entities.** { *; }
-keep class com.example.data.models.** { *; }

# AndroidX Room
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Firebase & Play Services
-keepattributes *Annotation*
-keepclassmembers class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

