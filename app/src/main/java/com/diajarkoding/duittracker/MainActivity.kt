package com.diajarkoding.duittracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.LaunchedEffect
import com.diajarkoding.duittracker.ui.navigation.DuitTrackerNavGraph
import com.diajarkoding.duittracker.ui.theme.DuitTrackerTheme
import com.diajarkoding.duittracker.utils.LocaleManager
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        Log.d(TAG, "attachBaseContext() called")
        super.attachBaseContext(LocaleManager.attachBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() called")
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Log.d(TAG, "setContent() - Locale.getDefault(): ${java.util.Locale.getDefault()}")
            
            // Observe configurationVersion to trigger recomposition on language change  
            // Reading this state creates a dependency that triggers recomposition
            val configVersion = LocaleManager.configurationVersion
            Log.d(TAG, "setContent() - configVersion: $configVersion")

            // Update Activity locale when configVersion changes
            LaunchedEffect(configVersion) {
                if (configVersion > 0) {
                    LocaleManager.updateActivityLocale(this@MainActivity)
                }
            }

            // Don't use key() here - it resets navigation to splash screen
            // Instead rely on state observation for recomposition
            DuitTrackerTheme {
                DuitTrackerNavGraph()
            }
        }
    }
}
