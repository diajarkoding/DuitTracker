 package com.diajarkoding.duittracker.utils
 
 import android.content.Context
 import android.content.SharedPreferences
 import android.content.res.Configuration
 import android.os.Build
 import android.os.LocaleList
 import androidx.compose.runtime.getValue
 import androidx.compose.runtime.mutableIntStateOf
 import androidx.compose.runtime.setValue
 import com.diajarkoding.duittracker.data.local.preferences.AppLanguage
 import java.util.Locale
 
 /**
  * LocaleManager - Manages app localization without Activity.recreate()
  * 
  * Uses Compose state (configurationVersion) to trigger recomposition
  * when language changes, avoiding flicker/black screen.
  */
 object LocaleManager {
     
     private const val PREFS_NAME = "locale_prefs"
     private const val KEY_LANGUAGE = "language_code"
     private const val DEFAULT_LANGUAGE = "en"
     
     private lateinit var prefs: SharedPreferences
     private lateinit var appContext: Context
     private var currentLanguageCode: String = DEFAULT_LANGUAGE
     
     /**
      * Compose state that triggers recomposition when language changes.
      * Observe this in your root composable to react to language changes.
      */
     var configurationVersion by mutableIntStateOf(0)
         private set
     
     /**
      * Initialize LocaleManager. Call this in Application.onCreate()
      */
     fun init(context: Context) {
         appContext = context.applicationContext
         prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
         currentLanguageCode = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
         updateLocale()
     }
     
     /**
      * Get current language
      */
     fun getCurrentLanguage(): AppLanguage {
         return AppLanguage.entries.find { it.code == currentLanguageCode } ?: AppLanguage.ENGLISH
     }
     
     /**
      * Set language and trigger UI update
      */
     fun setLanguage(context: Context, language: AppLanguage) {
         if (currentLanguageCode != language.code) {
             currentLanguageCode = language.code
             prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
             updateLocale(context)
             // Increment to trigger Compose recomposition
             configurationVersion++
         }
     }
     
     private fun createLocale(languageCode: String): Locale {
         return Locale.forLanguageTag(languageCode)
     }
     
     /**
      * Toggle between English and Indonesian
      */
     fun toggleLanguage(context: Context) {
         val newLanguage = if (currentLanguageCode == "en") {
             AppLanguage.INDONESIAN
         } else {
             AppLanguage.ENGLISH
         }
         setLanguage(context, newLanguage)
     }
     
     /**
      * Update the locale configuration
      */
     private fun updateLocale(context: Context? = null) {
         val locale = createLocale(currentLanguageCode)
         Locale.setDefault(locale)
         
         // Update Application context resources
         if (::appContext.isInitialized) {
             val appConfig = Configuration(appContext.resources.configuration)
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                 val localeList = LocaleList(locale)
                 LocaleList.setDefault(localeList)
                 appConfig.setLocales(localeList)
             } else {
                 @Suppress("DEPRECATION")
                 appConfig.locale = locale
             }
             appConfig.setLayoutDirection(locale)
             @Suppress("DEPRECATION")
             appContext.resources.updateConfiguration(appConfig, appContext.resources.displayMetrics)
         }
         
         // Also update the passed context (Activity) resources
         context?.let { ctx ->
             val activityConfig = Configuration(ctx.resources.configuration)
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                 val localeList = LocaleList(locale)
                 activityConfig.setLocales(localeList)
             } else {
                 @Suppress("DEPRECATION")
                 activityConfig.locale = locale
             }
             activityConfig.setLayoutDirection(locale)
             @Suppress("DEPRECATION")
             ctx.resources.updateConfiguration(activityConfig, ctx.resources.displayMetrics)
         }
     }
     
     /**
      * Update locale for a specific context (call from Activity)
      */
     fun updateActivityLocale(context: Context) {
         val locale = createLocale(currentLanguageCode)
         val config = Configuration(context.resources.configuration)
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
             val localeList = LocaleList(locale)
             config.setLocales(localeList)
         } else {
             @Suppress("DEPRECATION")
             config.locale = locale
         }
         config.setLayoutDirection(locale)
         @Suppress("DEPRECATION")
         context.resources.updateConfiguration(config, context.resources.displayMetrics)
     }
     
     /**
      * Wrap context with current locale. Call this in Activity.attachBaseContext()
      */
     fun attachBaseContext(context: Context): Context {
         // Initialize if not already done
         if (!::prefs.isInitialized) {
             prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
             currentLanguageCode = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
         }
         
         val locale = createLocale(currentLanguageCode)
         Locale.setDefault(locale)
         
         val config = Configuration(context.resources.configuration)
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
             val localeList = LocaleList(locale)
             LocaleList.setDefault(localeList)
             config.setLocales(localeList)
         } else {
             @Suppress("DEPRECATION")
             config.locale = locale
         }
         config.setLayoutDirection(locale)
         
         return context.createConfigurationContext(config)
     }
 }
