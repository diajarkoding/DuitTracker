 package com.diajarkoding.duittracker.ui.components
 
 import androidx.compose.foundation.layout.Arrangement
 import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.BoxWithConstraints
 import androidx.compose.foundation.layout.PaddingValues
 import androidx.compose.foundation.layout.fillMaxSize
 import androidx.compose.foundation.layout.fillMaxWidth
 import androidx.compose.foundation.layout.padding
 import androidx.compose.foundation.layout.widthIn
 import androidx.compose.foundation.lazy.LazyColumn
 import androidx.compose.foundation.lazy.LazyListScope
 import androidx.compose.foundation.lazy.grid.GridCells
 import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
 import androidx.compose.foundation.lazy.grid.LazyGridScope
 import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
 import androidx.compose.runtime.Composable
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.unit.Dp
 import androidx.compose.ui.unit.dp
 import com.diajarkoding.duittracker.ui.theme.NeoSpacing
 import com.diajarkoding.duittracker.ui.utils.LocalWindowSizeClass
 
 @Composable
 fun AdaptiveContainer(
     modifier: Modifier = Modifier,
     maxWidth: Dp = 600.dp,
     content: @Composable () -> Unit
 ) {
     Box(
         modifier = modifier.fillMaxSize(),
         contentAlignment = Alignment.TopCenter
     ) {
         Box(
             modifier = Modifier.widthIn(max = maxWidth)
         ) {
             content()
         }
     }
 }
 
 @Composable
 fun AdaptiveLazyColumn(
     modifier: Modifier = Modifier,
     contentPadding: PaddingValues = PaddingValues(0.dp),
     verticalArrangement: Arrangement.Vertical = Arrangement.Top,
     horizontalAlignment: Alignment.Horizontal = Alignment.Start,
     maxWidth: Dp = 600.dp,
     content: LazyListScope.() -> Unit
 ) {
     Box(
         modifier = modifier.fillMaxSize(),
         contentAlignment = Alignment.TopCenter
     ) {
         LazyColumn(
             modifier = Modifier.widthIn(max = maxWidth),
             contentPadding = contentPadding,
             verticalArrangement = verticalArrangement,
             horizontalAlignment = horizontalAlignment,
             content = content
         )
     }
 }
 
 @Composable
 fun ResponsiveGrid(
     modifier: Modifier = Modifier,
     minColumnWidth: Dp = 300.dp,
     contentPadding: PaddingValues = PaddingValues(NeoSpacing.lg),
     verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(NeoSpacing.md),
     horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(NeoSpacing.md),
     content: LazyGridScope.() -> Unit
 ) {
     LazyVerticalGrid(
         columns = GridCells.Adaptive(minSize = minColumnWidth),
         modifier = modifier.fillMaxSize(),
         contentPadding = contentPadding,
         verticalArrangement = verticalArrangement,
         horizontalArrangement = horizontalArrangement,
         content = content
     )
 }
 
 @Composable
 fun getAdaptiveColumns(): Int {
    val windowSizeClass = LocalWindowSizeClass.current
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        WindowWidthSizeClass.Expanded -> 3
        else -> 1
     }
 }
 
 @Composable
 fun getAdaptiveHorizontalPadding(): Dp {
    val windowSizeClass = LocalWindowSizeClass.current
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> NeoSpacing.lg
        WindowWidthSizeClass.Medium -> NeoSpacing.xl
        WindowWidthSizeClass.Expanded -> NeoSpacing.xxxl
        else -> NeoSpacing.lg
     }
 }
