 package com.diajarkoding.duittracker.ui.features.profile

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
 import androidx.compose.material.icons.filled.BarChart
 import androidx.compose.material.icons.filled.ChevronRight
 import androidx.compose.material3.AlertDialog
 import androidx.compose.material3.Icon
 import androidx.compose.material3.MaterialTheme
 import androidx.compose.material3.Scaffold
 import androidx.compose.material3.SnackbarHostState
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
 import com.diajarkoding.duittracker.ui.components.NeoAvatar
 import com.diajarkoding.duittracker.ui.components.NeoButtonText
 import com.diajarkoding.duittracker.ui.components.NeoCard
 import com.diajarkoding.duittracker.ui.components.NeoCardFlat
 import com.diajarkoding.duittracker.ui.components.NeoIconButton
 import com.diajarkoding.duittracker.ui.components.NeoSnackbarHost
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
 
     LaunchedEffect(Unit) {
         viewModel.events.collectLatest { event ->
             when (event) {
                 is ProfileEvent.LoggedOut -> onLogout()
                 is ProfileEvent.ShowSnackbar -> {
                     snackbarHostState.showNeoSnackbar(event.message, event.type)
                 }
                is ProfileEvent.LanguageChanged -> { }
             }
         }
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
 
            // TODO: Language Section - Hidden temporarily
            // TODO: Reminder Section - Hidden temporarily
 
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
