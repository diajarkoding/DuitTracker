package com.diajarkoding.duittracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.diajarkoding.duittracker.data.local.preferences.AppLanguage
import com.diajarkoding.duittracker.data.local.preferences.AppPreferences
import com.diajarkoding.duittracker.data.local.preferences.appPreferencesDataStore
import com.diajarkoding.duittracker.ui.navigation.DuitTrackerNavGraph
import com.diajarkoding.duittracker.ui.theme.DuitTrackerTheme
import com.diajarkoding.duittracker.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun attachBaseContext(newBase: Context) {
        Log.d(TAG, "attachBaseContext() called")
        // Read language preference synchronously from DataStore
        val languageCode = runBlocking {
            newBase.appPreferencesDataStore.data.map { preferences ->
                preferences[AppPreferences.LANGUAGE_KEY] ?: AppLanguage.ENGLISH.code
            }.first()
        }
        Log.d(TAG, "attachBaseContext() - languageCode from DataStore: $languageCode")
        val language = LocaleHelper.getLanguageFromCode(languageCode)
        Log.d(TAG, "attachBaseContext() - language: ${language.code}, ${language.displayName}")
        val context = LocaleHelper.wrapContext(newBase, language)
        Log.d(TAG, "attachBaseContext() - wrapped context locale: ${context.resources.configuration.locales[0]}")
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() called")
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Log.d(TAG, "setContent() - Locale.getDefault(): ${java.util.Locale.getDefault()}")

            DuitTrackerTheme {
                val navController = rememberNavController()
                DuitTrackerNavGraph(navController = navController)
            }
        }
    }
}
