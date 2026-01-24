package com.diajarkoding.duittracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NeoLightColorScheme = lightColorScheme(
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

private val NeoDarkColorScheme = darkColorScheme(
    primary = NeoPrimaryDark,
    onPrimary = NeoOnPrimaryDark,
    secondary = NeoSecondaryDark,
    onSecondary = NeoOnSecondaryDark,
    tertiary = NeoTertiaryDark,
    onTertiary = NeoOnTertiaryDark,
    background = NeoBackgroundDark,
    onBackground = NeoOnBackgroundDark,
    surface = NeoSurfaceDark,
    onSurface = NeoOnSurfaceDark,
    error = NeoErrorDark,
    onError = NeoOnErrorDark,
    surfaceVariant = NeoColors.DarkCard,
    onSurfaceVariant = NeoColors.DarkOnSurface,
    outline = NeoColors.DarkMediumGray,
    outlineVariant = NeoColors.DarkLightGray
)

/**
 * Comprehensive theme colors for Neobrutalism design system.
 * Contains all colors needed for both light and dark themes.
 */
data class NeoThemeColors(
    // Background colors
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    
    // Text colors  
    val textPrimary: Color,
    val textSecondary: Color,
    val textOnAccent: Color,
    
    // Border and shadow
    val border: Color,
    val shadow: Color,
    
    // Neutral colors
    val pureBlack: Color,
    val pureWhite: Color,
    val mediumGray: Color,
    val lightGray: Color,
    val darkGray: Color,
    
    // Semantic colors (sama untuk light/dark)
    val incomeGreen: Color = NeoColors.IncomeGreen,
    val expenseRed: Color = NeoColors.ExpenseRed,
    val electricBlue: Color = NeoColors.ElectricBlue,
    val deepPurple: Color = NeoColors.DeepPurple,
    val vividOrange: Color = NeoColors.VividOrange,
    val sunYellow: Color = NeoColors.SunYellow,
    val hotPink: Color = NeoColors.HotPink,
    val limeGreen: Color = NeoColors.LimeGreen,
    
    // State
    val isDark: Boolean
)

val LightNeoThemeColors = NeoThemeColors(
    background = NeoColors.Background,
    surface = NeoColors.PureWhite,
    cardBackground = NeoColors.PureWhite,
    textPrimary = NeoColors.PureBlack,
    textSecondary = NeoColors.MediumGray,
    textOnAccent = NeoColors.PureWhite,
    border = NeoColors.PureBlack,
    shadow = NeoColors.PureBlack,
    pureBlack = NeoColors.PureBlack,
    pureWhite = NeoColors.PureWhite,
    mediumGray = NeoColors.MediumGray,
    lightGray = NeoColors.LightGray,
    darkGray = NeoColors.DarkGray,
    isDark = false
)

val DarkNeoThemeColors = NeoThemeColors(
    background = NeoColors.DarkBackground,
    surface = NeoColors.DarkSurface,
    cardBackground = NeoColors.DarkCard,
    textPrimary = NeoColors.DarkOnSurface,
    textSecondary = NeoColors.DarkMediumGray,
    textOnAccent = NeoColors.PureWhite,
    border = NeoColors.DarkLightGray,
    shadow = Color(0xFF000000),
    pureBlack = NeoColors.DarkOnSurface,  // Inverted for dark mode
    pureWhite = NeoColors.DarkCard,        // Inverted for dark mode
    mediumGray = NeoColors.DarkMediumGray,
    lightGray = NeoColors.DarkLightGray,
    darkGray = NeoColors.DarkOnBackground,
    isDark = true
)

val LocalNeoThemeColors = staticCompositionLocalOf { LightNeoThemeColors }

@Composable
fun DuitTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NeoDarkColorScheme else NeoLightColorScheme
    val neoColors = if (darkTheme) DarkNeoThemeColors else LightNeoThemeColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = neoColors.background.toArgb()
            window.navigationBarColor = neoColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalNeoThemeColors provides neoColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NeoTypography,
            content = content
        )
    }
}

/**
 * Access theme colors throughout the app.
 * Usage: NeoTheme.colors.background, NeoTheme.colors.textPrimary, etc.
 */
object NeoTheme {
    val colors: NeoThemeColors
        @Composable
        get() = LocalNeoThemeColors.current
}