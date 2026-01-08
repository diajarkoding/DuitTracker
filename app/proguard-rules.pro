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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============================================
# DuitTracker ProGuard Rules
# ============================================

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# Kotlin Serialization
# ============================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.diajarkoding.duittracker.**$$serializer { *; }
-keepclassmembers class com.diajarkoding.duittracker.** {
    *** Companion;
}
-keepclasseswithmembers class com.diajarkoding.duittracker.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================
# Supabase / Ktor
# ============================================
-keep class io.ktor.** { *; }
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.ktor.**
-dontwarn io.github.jan.supabase.**

# ============================================
# Room Database
# ============================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ============================================
# Hilt / Dagger
# ============================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <fields>;
}

# ============================================
# Apache POI (Excel export)
# ============================================
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.apache.commons.**
-dontwarn org.openxmlformats.**
-dontwarn com.microsoft.schemas.**
-dontwarn org.etsi.**
-dontwarn org.w3.**
-dontwarn aQute.bnd.annotation.**
-dontwarn org.osgi.**
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn org.apache.batik.**
-dontwarn org.apache.logging.log4j.**
-dontwarn com.graphbuilder.**
-dontwarn org.bouncycastle.**
-dontwarn org.apache.pdfbox.**
-dontwarn de.rototor.**
-dontwarn org.apache.fontbox.**
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }

# ============================================
# Vico Charts
# ============================================
-keep class com.patrykandpatrick.vico.** { *; }

# ============================================
# Data classes and DTOs
# ============================================
-keep class com.diajarkoding.duittracker.data.model.** { *; }
-keep class com.diajarkoding.duittracker.data.remote.dto.** { *; }
-keep class com.diajarkoding.duittracker.data.local.entity.** { *; }

# ============================================
# Kotlinx DateTime
# ============================================
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.datetime.**

# ============================================
# Coil
# ============================================
-keep class coil.** { *; }
-dontwarn coil.**