 package com.diajarkoding.duittracker.ui.features.theme
 
 import androidx.compose.foundation.background
 import androidx.compose.foundation.clickable
 import androidx.compose.foundation.layout.Arrangement
 import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.Row
 import androidx.compose.foundation.layout.Spacer
 import androidx.compose.foundation.layout.fillMaxSize
 import androidx.compose.foundation.layout.fillMaxWidth
 import androidx.compose.foundation.layout.height
 import androidx.compose.foundation.layout.padding
 import androidx.compose.foundation.layout.size
 import androidx.compose.foundation.layout.systemBarsPadding
 import androidx.compose.foundation.layout.width
 import androidx.compose.foundation.rememberScrollState
 import androidx.compose.foundation.shape.RoundedCornerShape
 import androidx.compose.foundation.verticalScroll
 import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.automirrored.filled.ArrowBack
 import androidx.compose.material.icons.filled.Check
 import androidx.compose.material.icons.filled.DarkMode
 import androidx.compose.material.icons.filled.LightMode
 import androidx.compose.material.icons.filled.Palette
 import androidx.compose.material.icons.filled.SettingsBrightness
 import androidx.compose.material3.Icon
 import androidx.compose.material3.MaterialTheme
 import androidx.compose.material3.Scaffold
 import androidx.compose.material3.Text
 import androidx.compose.runtime.Composable
 import androidx.compose.runtime.getValue
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.draw.clip
 import androidx.compose.ui.graphics.vector.ImageVector
 import androidx.compose.ui.res.stringResource
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.tooling.preview.Preview
 import androidx.compose.ui.unit.dp
 import androidx.hilt.navigation.compose.hiltViewModel
 import androidx.lifecycle.compose.collectAsStateWithLifecycle
 import com.diajarkoding.duittracker.R
 import com.diajarkoding.duittracker.data.local.preferences.ThemeMode
 import com.diajarkoding.duittracker.ui.components.NeoCard
 import com.diajarkoding.duittracker.ui.components.NeoCardFlat
 import com.diajarkoding.duittracker.ui.components.NeoIconButton
 import com.diajarkoding.duittracker.ui.theme.DuitTrackerTheme
 import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoTheme
 import com.diajarkoding.duittracker.ui.theme.NeoDimens
 import com.diajarkoding.duittracker.ui.theme.NeoSpacing
 
 @Composable
 fun ThemeScreen(
     onNavigateBack: () -> Unit,
     viewModel: ThemeViewModel = hiltViewModel()
 ) {
     val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
 
     Scaffold(
         topBar = {
             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .systemBarsPadding()
                     .padding(horizontal = NeoSpacing.lg, vertical = NeoSpacing.md),
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 NeoIconButton(
                     onClick = onNavigateBack,
                     backgroundColor = NeoTheme.colors.cardBackground
                 ) {
                     Icon(
                         imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                         contentDescription = stringResource(R.string.back),
                         modifier = Modifier.size(NeoDimens.iconSizeMedium)
                     )
                 }
                 Spacer(modifier = Modifier.width(NeoSpacing.md))
                 Text(
                     text = stringResource(R.string.theme),
                     style = MaterialTheme.typography.titleLarge,
                     fontWeight = FontWeight.Bold,
                     color = NeoTheme.colors.textPrimary
                 )
             }
         },
         containerColor = NeoTheme.colors.background
     ) { paddingValues ->
         Column(
             modifier = Modifier
                 .fillMaxSize()
                 .padding(paddingValues)
                 .padding(horizontal = NeoSpacing.lg)
                 .verticalScroll(rememberScrollState())
         ) {
             Spacer(modifier = Modifier.height(NeoSpacing.lg))
 
             // Header Card
             NeoCard(
                 modifier = Modifier.fillMaxWidth(),
                 backgroundColor = NeoColors.ElectricBlue,
                 shadowOffset = NeoDimens.shadowOffset,
                 cornerRadius = NeoDimens.cornerRadius
             ) {
                 Row(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(NeoSpacing.xl),
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     Box(
                         modifier = Modifier
                             .size(56.dp)
                             .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                             .background(NeoColors.PureWhite.copy(alpha = 0.2f)),
                         contentAlignment = Alignment.Center
                     ) {
                         Icon(
                             imageVector = Icons.Default.Palette,
                             contentDescription = null,
                             tint = NeoColors.PureWhite,
                             modifier = Modifier.size(32.dp)
                         )
                     }
                     Spacer(modifier = Modifier.width(NeoSpacing.lg))
                     Column {
                         Text(
                             text = stringResource(R.string.theme_settings),
                             style = MaterialTheme.typography.titleMedium,
                             fontWeight = FontWeight.Bold,
                             color = NeoColors.PureWhite
                         )
                         Spacer(modifier = Modifier.height(NeoSpacing.xs))
                         Text(
                             text = stringResource(R.string.theme_description),
                             style = MaterialTheme.typography.bodySmall,
                             color = NeoColors.PureWhite.copy(alpha = 0.8f)
                         )
                     }
                 }
             }
 
             Spacer(modifier = Modifier.height(NeoSpacing.xl))
 
             Text(
                 text = stringResource(R.string.select_theme),
                 style = MaterialTheme.typography.labelMedium,
                 fontWeight = FontWeight.SemiBold,
                 color = NeoTheme.colors.textSecondary
             )
 
             Spacer(modifier = Modifier.height(NeoSpacing.md))
 
             // Theme Options
             ThemeOptionItem(
                 icon = Icons.Default.SettingsBrightness,
                 title = stringResource(R.string.theme_system),
                 subtitle = stringResource(R.string.theme_system_desc),
                 isSelected = currentTheme == ThemeMode.SYSTEM,
                 onClick = { viewModel.setTheme(ThemeMode.SYSTEM) }
             )
 
             Spacer(modifier = Modifier.height(NeoSpacing.md))
 
             ThemeOptionItem(
                 icon = Icons.Default.LightMode,
                 title = stringResource(R.string.theme_light),
                 subtitle = stringResource(R.string.theme_light_desc),
                 isSelected = currentTheme == ThemeMode.LIGHT,
                 onClick = { viewModel.setTheme(ThemeMode.LIGHT) }
             )
 
             Spacer(modifier = Modifier.height(NeoSpacing.md))
 
             ThemeOptionItem(
                 icon = Icons.Default.DarkMode,
                 title = stringResource(R.string.theme_dark),
                 subtitle = stringResource(R.string.theme_dark_desc),
                 isSelected = currentTheme == ThemeMode.DARK,
                 onClick = { viewModel.setTheme(ThemeMode.DARK) }
             )
 
             Spacer(modifier = Modifier.height(NeoSpacing.xxl))
         }
     }
 }
 
 @Composable
 private fun ThemeOptionItem(
     icon: ImageVector,
     title: String,
     subtitle: String,
     isSelected: Boolean,
     onClick: () -> Unit
 ) {
     NeoCardFlat(
         modifier = Modifier
             .fillMaxWidth()
             .clickable(onClick = onClick),
         backgroundColor = if (isSelected) NeoColors.ElectricBlue else NeoTheme.colors.cardBackground,
         cornerRadius = NeoDimens.cornerRadius
     ) {
         Row(
             modifier = Modifier
                 .fillMaxWidth()
                 .padding(NeoSpacing.lg),
             verticalAlignment = Alignment.CenterVertically,
             horizontalArrangement = Arrangement.SpaceBetween
         ) {
             Row(
                 verticalAlignment = Alignment.CenterVertically,
                 modifier = Modifier.weight(1f)
             ) {
                 Box(
                     modifier = Modifier
                         .size(44.dp)
                         .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                         .background(
                             if (isSelected) NeoTheme.colors.cardBackground.copy(alpha = 0.2f)
                             else NeoTheme.colors.lightGray
                         ),
                     contentAlignment = Alignment.Center
                 ) {
                     Icon(
                         imageVector = icon,
                         contentDescription = null,
                         tint = if (isSelected) NeoTheme.colors.cardBackground else NeoTheme.colors.textPrimary,
                         modifier = Modifier.size(24.dp)
                     )
                 }
                 Spacer(modifier = Modifier.width(NeoSpacing.md))
                 Column {
                     Text(
                         text = title,
                         style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.SemiBold,
                         color = if (isSelected) NeoTheme.colors.cardBackground else NeoTheme.colors.textPrimary
                     )
                     Text(
                         text = subtitle,
                         style = MaterialTheme.typography.bodySmall,
                         color = if (isSelected) NeoTheme.colors.cardBackground.copy(alpha = 0.8f) else NeoTheme.colors.textSecondary
                     )
                 }
             }
             if (isSelected) {
                 Box(
                     modifier = Modifier
                         .size(28.dp)
                         .clip(RoundedCornerShape(14.dp))
                         .background(NeoTheme.colors.cardBackground),
                     contentAlignment = Alignment.Center
                 ) {
                     Icon(
                         imageVector = Icons.Default.Check,
                         contentDescription = null,
                         tint = NeoColors.ElectricBlue,
                         modifier = Modifier.size(18.dp)
                     )
                 }
             }
         }
     }
 }
 
 @Preview(showBackground = true, name = "Theme Option - System Selected")
 @Composable
 private fun ThemeOptionSystemSelectedPreview() {
     DuitTrackerTheme {
         ThemeOptionItem(
             icon = Icons.Default.SettingsBrightness,
             title = "System",
             subtitle = "Follow system theme",
             isSelected = true,
             onClick = {}
         )
     }
 }
 
 @Preview(showBackground = true, name = "Theme Option - Light Unselected")
 @Composable
 private fun ThemeOptionLightUnselectedPreview() {
     DuitTrackerTheme {
         ThemeOptionItem(
             icon = Icons.Default.LightMode,
             title = "Light",
             subtitle = "Always light theme",
             isSelected = false,
             onClick = {}
         )
     }
 }
 
 @Preview(showBackground = true, name = "Theme Option - Dark Unselected")
 @Composable
 private fun ThemeOptionDarkUnselectedPreview() {
     DuitTrackerTheme {
         ThemeOptionItem(
             icon = Icons.Default.DarkMode,
             title = "Dark",
             subtitle = "Always dark theme",
             isSelected = false,
             onClick = {}
         )
     }
 }
