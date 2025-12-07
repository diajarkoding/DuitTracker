package com.diajarkoding.duittracker.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Neobrutalism Color Palette
object NeoColors {
    // Primary Accent
    val Primary = Color(0xFF000000)
    val Accent = Color(0xFFFFE500)
    
    // Softer Primary Colors
    val ElectricBlue = Color(0xFF2563EB)
    val LimeGreen = Color(0xFF22C55E)
    val HotPink = Color(0xFFEC4899)
    val SunYellow = Color(0xFFFFE500)
    val VividOrange = Color(0xFFF97316)
    val DeepPurple = Color(0xFF8B5CF6)

    // Background & Surface - Clean and minimal
    val Background = Color(0xFFF5F5F4)
    val OffWhite = Color(0xFFFAFAF9)
    val PaperWhite = Color(0xFFFFFFF5)
    val PureWhite = Color(0xFFFFFFFF)
    val CardBackground = Color(0xFFFFFFFF)

    // Stroke & Shadows
    val PureBlack = Color(0xFF000000)
    val DarkGray = Color(0xFF1A1A1A)
    val MediumGray = Color(0xFF525252)
    val LightGray = Color(0xFFD4D4D4)

    // Semantic Colors
    val ExpenseRed = Color(0xFFEF4444)
    val IncomeGreen = Color(0xFF22C55E)

    // Category Colors
    val FoodOrange = Color(0xFFF97316)
    val TransportBlue = Color(0xFF3B82F6)
    val ShoppingPink = Color(0xFFEC4899)
    val EntertainmentPurple = Color(0xFF8B5CF6)
    val BillsRed = Color(0xFFEF4444)
    val HealthGreen = Color(0xFF14B8A6)
    val EducationYellow = Color(0xFFEAB308)
    val OtherGray = Color(0xFF6B7280)
}

// Consistent Spacing System
object NeoSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

// Consistent Border & Shadow
object NeoDimens {
    val borderWidth = 2.dp
    val borderWidthBold = 3.dp
    val shadowOffset = 4.dp
    val shadowOffsetLarge = 6.dp
    val cornerRadius = 12.dp
    val cornerRadiusSmall = 8.dp
    val iconSizeSmall = 20.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 32.dp
}

// Material3 Color Scheme mappings
val NeoPrimary = NeoColors.ElectricBlue
val NeoSecondary = NeoColors.LimeGreen
val NeoTertiary = NeoColors.HotPink
val NeoBackground = NeoColors.OffWhite
val NeoSurface = NeoColors.PureWhite
val NeoOnPrimary = NeoColors.PureWhite
val NeoOnSecondary = NeoColors.PureBlack
val NeoOnTertiary = NeoColors.PureWhite
val NeoOnBackground = NeoColors.PureBlack
val NeoOnSurface = NeoColors.PureBlack
val NeoError = NeoColors.ExpenseRed
val NeoOnError = NeoColors.PureWhite
