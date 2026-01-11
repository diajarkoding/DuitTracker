 package com.diajarkoding.duittracker.utils
 
 import android.content.Context
 import android.content.res.Configuration
 import android.os.Build
 import android.os.LocaleList
import android.util.Log
 import com.diajarkoding.duittracker.data.local.preferences.AppLanguage
 import java.util.Locale
 
 object LocaleHelper {
 
    private const val TAG = "LocaleHelper"

     fun wrapContext(context: Context, language: AppLanguage): Context {
        Log.d(TAG, "wrapContext() called - language: ${language.code}")
        @Suppress("DEPRECATION")
        val locale = Locale(language.code)
        Log.d(TAG, "wrapContext() - Created Locale: $locale")
         Locale.setDefault(locale)
        Log.d(TAG, "wrapContext() - Locale.getDefault() after setDefault: ${Locale.getDefault()}")
         return updateResources(context, locale)
     }
 
     private fun updateResources(context: Context, locale: Locale): Context {
        Log.d(TAG, "updateResources() - locale: $locale")
         val configuration = Configuration(context.resources.configuration)
         
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
             val localeList = LocaleList(locale)
             LocaleList.setDefault(localeList)
             configuration.setLocales(localeList)
            Log.d(TAG, "updateResources() - API >= N, set LocaleList: $localeList")
         } else {
             @Suppress("DEPRECATION")
             configuration.locale = locale
            Log.d(TAG, "updateResources() - API < N, set configuration.locale: $locale")
         }
         
         configuration.setLayoutDirection(locale)
         
        val newContext = context.createConfigurationContext(configuration)
        Log.d(TAG, "updateResources() - New context created, config locales: ${newContext.resources.configuration.locales}")
        return newContext
     }
 
     fun getLanguageFromCode(code: String): AppLanguage {
        val result = AppLanguage.entries.find { it.code == code } ?: AppLanguage.ENGLISH
        Log.d(TAG, "getLanguageFromCode() - code: $code, result: ${result.code}")
        return result
     }
 }
