package com.diajarkoding.duittracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NeoColorScheme = lightColorScheme(
    primary = NeoPrimary,
    onPrimary = NeoOnPrimary,
    secondary = NeoSecondary,
    onSecondary = NeoOnSecondary,
    tertiary = NeoTertiary,
    onTertiary = NeoOnTertiary,
    background = NeoBackground,
    onBackground = NeoOnBackground,
    surface = NeoSurface,
    onSurface = NeoOnSurface,
    error = NeoError,
    onError = NeoOnError,
    surfaceVariant = NeoBackground,
    onSurfaceVariant = NeoOnSurface,
    outline = NeoColors.PureBlack,
    outlineVariant = NeoColors.DarkGray
)

@Composable
fun DuitTrackerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = NeoColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NeoBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NeoTypography,
        content = content
    )
}
