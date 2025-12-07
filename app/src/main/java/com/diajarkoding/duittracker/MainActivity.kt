package com.diajarkoding.duittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.diajarkoding.duittracker.ui.navigation.DuitTrackerNavGraph
import com.diajarkoding.duittracker.ui.theme.DuitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DuitTrackerTheme {
                val navController = rememberNavController()
                DuitTrackerNavGraph(navController = navController)
            }
        }
    }
}