package com.diajarkoding.duittracker.ui.features.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

private const val TAG = "ProfileScreen"

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToReminder: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    Log.d(TAG, "ProfileScreen composing - selectedLanguage: ${uiState.selectedLanguage.code}")
    Log.d(TAG, "ProfileScreen - current Locale.getDefault(): ${java.util.Locale.getDefault()}")
    Log.d(TAG, "ProfileScreen - context resources locale: ${context.resources.configuration.locales[0]}")

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileEvent.LoggedOut -> {
                    Log.d(TAG, "Event: LoggedOut")
                    onLogout()
                }
                is ProfileEvent.ShowSnackbar -> {
                    Log.d(TAG, "Event: ShowSnackbar - ${event.message}")
                    snackbarHostState.showNeoSnackbar(event.message, event.type)
                }
                else -> {}
            }
        }
    }

    if (uiState.showLogoutDialog) {
        NeoLogoutDialog(
            onDismiss = { viewModel.hideLogoutDialog() },
            onConfirm = { viewModel.logout() }
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

            // Language Menu
            ProfileMenuItem(
                icon = Icons.Default.Language,
                title = stringResource(R.string.language),
                iconBackgroundColor = NeoColors.DeepPurple,
                onClick = onNavigateToLanguage
            )

            Spacer(modifier = Modifier.height(NeoSpacing.md))

            // Reminder Menu
            ProfileMenuItem(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.daily_reminder),
                iconBackgroundColor = NeoColors.VividOrange,
                onClick = onNavigateToReminder
            )

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

@Composable
private fun NeoLogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier.padding(NeoSpacing.lg)
        ) {
            // Shadow layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = 4.dp, start = 4.dp)
                    .clip(RoundedCornerShape(NeoDimens.cornerRadius))
                    .background(NeoColors.PureBlack)
            )
            
            // Main dialog content
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(NeoDimens.cornerRadius))
                    .background(NeoColors.PureWhite)
                    .border(
                        width = NeoDimens.borderWidth,
                        color = NeoColors.PureBlack,
                        shape = RoundedCornerShape(NeoDimens.cornerRadius)
                    )
                    .padding(NeoSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                        .background(NeoColors.ExpenseRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = NeoColors.PureWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(NeoSpacing.lg))
                
                // Title
                Text(
                    text = stringResource(R.string.logout),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.PureBlack,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(NeoSpacing.sm))
                
                // Message
                Text(
                    text = stringResource(R.string.logout_confirmation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeoColors.MediumGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(NeoSpacing.xl))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md)
                ) {
                    // Cancel button
                    NeoButtonText(
                        text = stringResource(R.string.cancel),
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        backgroundColor = NeoColors.LightGray,
                        contentColor = NeoColors.PureBlack
                    )
                    
                    // Logout button
                    NeoButtonText(
                        text = stringResource(R.string.logout),
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        backgroundColor = NeoColors.ExpenseRed,
                        contentColor = NeoColors.PureWhite
                    )
                }
            }
        }
    }
}
