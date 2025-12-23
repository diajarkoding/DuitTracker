 package com.diajarkoding.duittracker.ui.features.profile
 
 import android.Manifest
 import android.os.Build
 import androidx.activity.compose.rememberLauncherForActivityResult
 import androidx.activity.result.contract.ActivityResultContracts
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
 import androidx.compose.material.icons.automirrored.filled.Logout
 import androidx.compose.material.icons.filled.BarChart
 import androidx.compose.material.icons.filled.ChevronRight
 import androidx.compose.material.icons.filled.Language
 import androidx.compose.material.icons.filled.Notifications
 import androidx.compose.material.icons.filled.Schedule
 import androidx.compose.material3.AlertDialog
 import androidx.compose.material3.Icon
 import androidx.compose.material3.MaterialTheme
 import androidx.compose.material3.Scaffold
 import androidx.compose.material3.SnackbarHostState
 import androidx.compose.material3.Switch
 import androidx.compose.material3.SwitchDefaults
 import androidx.compose.material3.Text
 import androidx.compose.material3.TextButton
 import androidx.compose.runtime.Composable
 import androidx.compose.runtime.LaunchedEffect
 import androidx.compose.runtime.getValue
 import androidx.compose.runtime.remember
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.draw.clip
 import androidx.compose.ui.graphics.vector.ImageVector
 import androidx.compose.ui.res.stringResource
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.unit.dp
 import androidx.hilt.navigation.compose.hiltViewModel
 import androidx.lifecycle.compose.collectAsStateWithLifecycle
 import com.diajarkoding.duittracker.R
 import com.diajarkoding.duittracker.data.local.preferences.AppLanguage
 import com.diajarkoding.duittracker.ui.components.NeoAvatar
 import com.diajarkoding.duittracker.ui.components.NeoButtonText
 import com.diajarkoding.duittracker.ui.components.NeoCard
 import com.diajarkoding.duittracker.ui.components.NeoCardFlat
 import com.diajarkoding.duittracker.ui.components.NeoIconButton
 import com.diajarkoding.duittracker.ui.components.NeoSnackbarHost
 import com.diajarkoding.duittracker.ui.components.NeoTimePickerDialog
 import com.diajarkoding.duittracker.ui.components.showNeoSnackbar
 import com.diajarkoding.duittracker.ui.theme.NeoColors
 import com.diajarkoding.duittracker.ui.theme.NeoDimens
 import com.diajarkoding.duittracker.ui.theme.NeoSpacing
 import kotlinx.coroutines.flow.collectLatest
 
 @Composable
 fun ProfileScreen(
     onNavigateBack: () -> Unit,
     onNavigateToStatistics: () -> Unit,
     onLogout: () -> Unit,
     viewModel: ProfileViewModel = hiltViewModel()
 ) {
     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
     val snackbarHostState = remember { SnackbarHostState() }
 
     val notificationPermissionLauncher = rememberLauncherForActivityResult(
         ActivityResultContracts.RequestPermission()
     ) { granted ->
         if (granted) {
             viewModel.setReminderEnabled(true)
         }
     }
 
     LaunchedEffect(Unit) {
         viewModel.events.collectLatest { event ->
             when (event) {
                 is ProfileEvent.LoggedOut -> onLogout()
                 is ProfileEvent.ShowSnackbar -> {
                     snackbarHostState.showNeoSnackbar(event.message, event.type)
                 }
                 is ProfileEvent.LanguageChanged -> {
                     // Language change will be handled by Activity recreation
                 }
             }
         }
     }
 
     if (uiState.showTimePickerDialog) {
         NeoTimePickerDialog(
             initialHour = uiState.reminderHour,
             initialMinute = uiState.reminderMinute,
             onTimeSelected = { hour, minute ->
                 viewModel.setReminderTime(hour, minute)
             },
             onDismiss = { viewModel.hideTimePickerDialog() }
         )
     }
 
     if (uiState.showLogoutDialog) {
         AlertDialog(
             onDismissRequest = { viewModel.hideLogoutDialog() },
             title = {
                 Text(
                     text = stringResource(R.string.logout),
                     fontWeight = FontWeight.Bold
                 )
             },
             text = {
                 Text(stringResource(R.string.logout_confirmation))
             },
             confirmButton = {
                 TextButton(onClick = { viewModel.logout() }) {
                     Text(
                         stringResource(R.string.logout),
                         color = NeoColors.ExpenseRed,
                         fontWeight = FontWeight.Bold
                     )
                 }
             },
             dismissButton = {
                 TextButton(onClick = { viewModel.hideLogoutDialog() }) {
                     Text(
                         stringResource(R.string.cancel),
                         color = NeoColors.PureBlack
                     )
                 }
             }
         )
     }
 
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
                     backgroundColor = NeoColors.PureWhite
                 ) {
                     Icon(
                         imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                         contentDescription = stringResource(R.string.back),
                         modifier = Modifier.size(NeoDimens.iconSizeMedium)
                     )
                 }
                 Spacer(modifier = Modifier.width(NeoSpacing.md))
                 Text(
                     text = stringResource(R.string.profile),
                     style = MaterialTheme.typography.titleLarge,
                     fontWeight = FontWeight.Bold,
                     color = NeoColors.PureBlack
                 )
             }
         },
         snackbarHost = { NeoSnackbarHost(snackbarHostState) },
         containerColor = NeoColors.Background
     ) { paddingValues ->
         Column(
             modifier = Modifier
                 .fillMaxSize()
                 .padding(paddingValues)
                 .padding(horizontal = NeoSpacing.lg)
                 .verticalScroll(rememberScrollState()),
             horizontalAlignment = Alignment.CenterHorizontally
         ) {
             Spacer(modifier = Modifier.height(NeoSpacing.xl))
 
             // Profile Card
             NeoCard(
                 modifier = Modifier.fillMaxWidth(),
                 backgroundColor = NeoColors.PureWhite,
                 shadowOffset = NeoDimens.shadowOffset,
                 cornerRadius = NeoDimens.cornerRadius
             ) {
                 Column(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(NeoSpacing.xl),
                     horizontalAlignment = Alignment.CenterHorizontally
                 ) {
                     NeoAvatar(
                         userName = uiState.userName,
                         avatarUrl = uiState.avatarUrl,
                         size = 80.dp
                     )
                     Spacer(modifier = Modifier.height(NeoSpacing.lg))
                     Text(
                         text = uiState.userName,
                         style = MaterialTheme.typography.titleLarge,
                         fontWeight = FontWeight.Bold,
                         color = NeoColors.PureBlack
                     )
                     Text(
                         text = uiState.email,
                         style = MaterialTheme.typography.bodyMedium,
                         color = NeoColors.MediumGray
                     )
                 }
             }
 
             Spacer(modifier = Modifier.height(NeoSpacing.xl))
 
             // Statistics Menu
             ProfileMenuItem(
                 icon = Icons.Default.BarChart,
                 title = stringResource(R.string.statistics),
                 iconBackgroundColor = NeoColors.ElectricBlue,
                 onClick = onNavigateToStatistics
             )
 
             Spacer(modifier = Modifier.height(NeoSpacing.md))
 
             // Language Section
             NeoCardFlat(
                 modifier = Modifier.fillMaxWidth(),
                 backgroundColor = NeoColors.PureWhite,
                 cornerRadius = NeoDimens.cornerRadius
             ) {
                 Column(
                     modifier = Modifier.padding(NeoSpacing.lg)
                 ) {
                     Row(
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Box(
                             modifier = Modifier
                                 .size(40.dp)
                                 .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                                 .background(NeoColors.DeepPurple),
                             contentAlignment = Alignment.Center
                         ) {
                             Icon(
                                 imageVector = Icons.Default.Language,
                                 contentDescription = null,
                                 tint = NeoColors.PureWhite,
                                 modifier = Modifier.size(NeoDimens.iconSizeMedium)
                             )
                         }
                         Spacer(modifier = Modifier.width(NeoSpacing.md))
                         Text(
                             text = stringResource(R.string.language),
                             style = MaterialTheme.typography.titleMedium,
                             fontWeight = FontWeight.SemiBold,
                             color = NeoColors.PureBlack
                         )
                     }
                     Spacer(modifier = Modifier.height(NeoSpacing.md))
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)
                     ) {
                         AppLanguage.entries.forEach { language ->
                             val isSelected = language == uiState.selectedLanguage
                             Box(
                                 modifier = Modifier
                                     .weight(1f)
                                     .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                                     .background(
                                         if (isSelected) NeoColors.PureBlack
                                         else NeoColors.LightGray.copy(alpha = 0.5f)
                                     )
                                     .clickable { viewModel.setLanguage(language) }
                                     .padding(vertical = NeoSpacing.md),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Text(
                                     text = language.displayName,
                                     style = MaterialTheme.typography.labelLarge,
                                     fontWeight = FontWeight.SemiBold,
                                     color = if (isSelected) NeoColors.PureWhite
                                     else NeoColors.MediumGray
                                 )
                             }
                         }
                     }
                 }
             }
 
             Spacer(modifier = Modifier.height(NeoSpacing.md))
 
             // Reminder Section
             NeoCardFlat(
                 modifier = Modifier.fillMaxWidth(),
                 backgroundColor = NeoColors.PureWhite,
                 cornerRadius = NeoDimens.cornerRadius
             ) {
                 Column(
                     modifier = Modifier.padding(NeoSpacing.lg)
                 ) {
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         verticalAlignment = Alignment.CenterVertically,
                         horizontalArrangement = Arrangement.SpaceBetween
                     ) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Box(
                                 modifier = Modifier
                                     .size(40.dp)
                                     .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                                     .background(NeoColors.VividOrange),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Icon(
                                     imageVector = Icons.Default.Notifications,
                                     contentDescription = null,
                                     tint = NeoColors.PureWhite,
                                     modifier = Modifier.size(NeoDimens.iconSizeMedium)
                                 )
                             }
                             Spacer(modifier = Modifier.width(NeoSpacing.md))
                             Text(
                                 text = stringResource(R.string.daily_reminder),
                                 style = MaterialTheme.typography.titleMedium,
                                 fontWeight = FontWeight.SemiBold,
                                 color = NeoColors.PureBlack
                             )
                         }
                         Switch(
                             checked = uiState.isReminderEnabled,
                             onCheckedChange = { enabled ->
                                 if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                     if (!viewModel.hasNotificationPermission()) {
                                         notificationPermissionLauncher.launch(
                                             Manifest.permission.POST_NOTIFICATIONS
                                         )
                                         return@Switch
                                     }
                                 }
                                 viewModel.setReminderEnabled(enabled)
                             },
                             colors = SwitchDefaults.colors(
                                 checkedThumbColor = NeoColors.PureWhite,
                                 checkedTrackColor = NeoColors.IncomeGreen,
                                 uncheckedThumbColor = NeoColors.PureWhite,
                                 uncheckedTrackColor = NeoColors.LightGray
                             )
                         )
                     }
                     if (uiState.isReminderEnabled) {
                         Spacer(modifier = Modifier.height(NeoSpacing.md))
                         Row(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                                 .background(NeoColors.LightGray.copy(alpha = 0.3f))
                                 .clickable { viewModel.showTimePickerDialog() }
                                 .padding(NeoSpacing.md),
                             verticalAlignment = Alignment.CenterVertically,
                             horizontalArrangement = Arrangement.SpaceBetween
                         ) {
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                 Icon(
                                     imageVector = Icons.Default.Schedule,
                                     contentDescription = null,
                                     tint = NeoColors.MediumGray,
                                     modifier = Modifier.size(NeoDimens.iconSizeMedium)
                                 )
                                 Spacer(modifier = Modifier.width(NeoSpacing.sm))
                                 Text(
                                     text = stringResource(R.string.reminder_time),
                                     style = MaterialTheme.typography.bodyMedium,
                                     color = NeoColors.MediumGray
                                 )
                             }
                             Text(
                                 text = String.format(
                                     "%02d:%02d",
                                     uiState.reminderHour,
                                     uiState.reminderMinute
                                 ),
                                 style = MaterialTheme.typography.titleMedium,
                                 fontWeight = FontWeight.Bold,
                                 color = NeoColors.PureBlack
                             )
                         }
                     }
                 }
             }
 
             Spacer(modifier = Modifier.height(NeoSpacing.xl))
 
             // Logout Button
             NeoButtonText(
                 text = stringResource(R.string.logout),
                 onClick = { viewModel.showLogoutDialog() },
                 modifier = Modifier.fillMaxWidth(),
                 backgroundColor = NeoColors.ExpenseRed,
                 contentColor = NeoColors.PureWhite
             )
 
             Spacer(modifier = Modifier.height(NeoSpacing.xxl))
         }
     }
 }
 
 @Composable
 private fun ProfileMenuItem(
     icon: ImageVector,
     title: String,
     iconBackgroundColor: androidx.compose.ui.graphics.Color,
     onClick: () -> Unit
 ) {
     NeoCardFlat(
         modifier = Modifier
             .fillMaxWidth()
             .clickable(onClick = onClick),
         backgroundColor = NeoColors.PureWhite,
         cornerRadius = NeoDimens.cornerRadius
     ) {
         Row(
             modifier = Modifier
                 .fillMaxWidth()
                 .padding(NeoSpacing.lg),
             verticalAlignment = Alignment.CenterVertically,
             horizontalArrangement = Arrangement.SpaceBetween
         ) {
             Row(verticalAlignment = Alignment.CenterVertically) {
                 Box(
                     modifier = Modifier
                         .size(40.dp)
                         .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                         .background(iconBackgroundColor),
                     contentAlignment = Alignment.Center
                 ) {
                     Icon(
                         imageVector = icon,
                         contentDescription = null,
                         tint = NeoColors.PureWhite,
                         modifier = Modifier.size(NeoDimens.iconSizeMedium)
                     )
                 }
                 Spacer(modifier = Modifier.width(NeoSpacing.md))
                 Text(
                     text = title,
                     style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.SemiBold,
                     color = NeoColors.PureBlack
                 )
             }
             Icon(
                 imageVector = Icons.Default.ChevronRight,
                 contentDescription = null,
                 tint = NeoColors.MediumGray,
                 modifier = Modifier.size(NeoDimens.iconSizeMedium)
             )
         }
     }
 }
