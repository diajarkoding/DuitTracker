 package com.diajarkoding.duittracker.ui.utils
 
 import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
 import androidx.compose.material3.windowsizeclass.WindowSizeClass
 import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
 import androidx.compose.runtime.Composable
 import androidx.compose.runtime.compositionLocalOf
 import androidx.compose.ui.unit.Dp
 import androidx.compose.ui.unit.dp
 
 val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> { 
     error("WindowSizeClass not provided") 
 }
 
 enum class DeviceType {
     PHONE_PORTRAIT,
     PHONE_LANDSCAPE,
     TABLET_PORTRAIT,
     TABLET_LANDSCAPE,
     DESKTOP
 }
 
 fun WindowSizeClass.getDeviceType(): DeviceType {
     return when {
         widthSizeClass == WindowWidthSizeClass.Compact -> DeviceType.PHONE_PORTRAIT
         widthSizeClass == WindowWidthSizeClass.Medium && 
             heightSizeClass == WindowHeightSizeClass.Compact -> DeviceType.PHONE_LANDSCAPE
         widthSizeClass == WindowWidthSizeClass.Medium -> DeviceType.TABLET_PORTRAIT
         widthSizeClass == WindowWidthSizeClass.Expanded && 
             heightSizeClass == WindowHeightSizeClass.Compact -> DeviceType.TABLET_LANDSCAPE
         widthSizeClass == WindowWidthSizeClass.Expanded -> DeviceType.DESKTOP
         else -> DeviceType.PHONE_PORTRAIT
     }
 }
 
 fun WindowSizeClass.isTablet(): Boolean {
     return widthSizeClass != WindowWidthSizeClass.Compact
 }
 
 fun WindowSizeClass.isLandscape(): Boolean {
     return heightSizeClass == WindowHeightSizeClass.Compact
 }
 
 data class AdaptiveLayoutConfig(
     val columns: Int,
     val horizontalPadding: Dp,
     val cardWidth: Dp?,
     val showNavigationRail: Boolean,
     val showBottomNav: Boolean
 )
 
 fun WindowSizeClass.getAdaptiveConfig(): AdaptiveLayoutConfig {
     return when (getDeviceType()) {
         DeviceType.PHONE_PORTRAIT -> AdaptiveLayoutConfig(
             columns = 1,
             horizontalPadding = 16.dp,
             cardWidth = null,
             showNavigationRail = false,
             showBottomNav = true
         )
         DeviceType.PHONE_LANDSCAPE -> AdaptiveLayoutConfig(
             columns = 2,
             horizontalPadding = 24.dp,
             cardWidth = null,
             showNavigationRail = false,
             showBottomNav = true
         )
         DeviceType.TABLET_PORTRAIT -> AdaptiveLayoutConfig(
             columns = 2,
             horizontalPadding = 32.dp,
             cardWidth = 400.dp,
             showNavigationRail = true,
             showBottomNav = false
         )
         DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP -> AdaptiveLayoutConfig(
             columns = 3,
             horizontalPadding = 48.dp,
             cardWidth = 350.dp,
             showNavigationRail = true,
             showBottomNav = false
         )
     }
 }
