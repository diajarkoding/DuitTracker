package com.diajarkoding.duittracker.ui.features.reminder

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diajarkoding.duittracker.BuildConfig
import com.diajarkoding.duittracker.R
import com.diajarkoding.duittracker.ui.components.NeoButtonText
import com.diajarkoding.duittracker.ui.components.NeoCard
import com.diajarkoding.duittracker.ui.components.NeoCardFlat
import com.diajarkoding.duittracker.ui.components.NeoIconButton
import com.diajarkoding.duittracker.ui.components.NeoSnackbarHost
import com.diajarkoding.duittracker.ui.components.showNeoSnackbar
import com.diajarkoding.duittracker.ui.features.profile.ProfileEvent
import com.diajarkoding.duittracker.ui.features.profile.ProfileViewModel
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ReminderScreen(
    onNavigateBack: () -> Unit,
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
                is ProfileEvent.ShowSnackbar -> {
                    snackbarHostState.showNeoSnackbar(event.message, event.type)
                }
                else -> {}
            }
        }
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
                    text = stringResource(R.string.daily_reminder),
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(NeoSpacing.lg))

            // Header Card
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NeoColors.VividOrange,
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
                            imageVector = if (uiState.isReminderEnabled) 
                                Icons.Default.NotificationsActive 
                            else 
                                Icons.Default.Notifications,
                            contentDescription = null,
                            tint = NeoColors.PureWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(NeoSpacing.lg))
                    Column {
                        Text(
                            text = stringResource(R.string.reminder_settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeoColors.PureWhite
                        )
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        Text(
                            text = stringResource(R.string.reminder_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeoColors.PureWhite.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(NeoSpacing.xl))

            // Enable/Disable Reminder
            NeoCardFlat(
                modifier = Modifier.fillMaxWidth(),
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
                                .background(
                                    if (uiState.isReminderEnabled) NeoColors.IncomeGreen 
                                    else NeoColors.LightGray
                                ),
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
                        Column {
                            Text(
                                text = stringResource(R.string.enable_reminder),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = NeoColors.PureBlack
                            )
                            Text(
                                text = if (uiState.isReminderEnabled) 
                                    stringResource(R.string.reminder_active) 
                                else 
                                    stringResource(R.string.reminder_inactive),
                                style = MaterialTheme.typography.bodySmall,
                                color = NeoColors.MediumGray
                            )
                        }
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
            }

            // Schedule Info (only shown when enabled)
            if (uiState.isReminderEnabled) {
                Spacer(modifier = Modifier.height(NeoSpacing.lg))

                Text(
                    text = stringResource(R.string.reminder_schedule),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NeoColors.MediumGray
                )

                Spacer(modifier = Modifier.height(NeoSpacing.md))

                // Lunch Reminder Card
                NeoCardFlat(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NeoColors.PureWhite,
                    cornerRadius = NeoDimens.cornerRadius
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(NeoSpacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                                .background(NeoColors.SunYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = NeoColors.PureBlack,
                                modifier = Modifier.size(NeoDimens.iconSizeMedium)
                            )
                        }
                        Spacer(modifier = Modifier.width(NeoSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.lunch_reminder),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = NeoColors.PureBlack
                            )
                            Text(
                                text = stringResource(R.string.lunch_reminder_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = NeoColors.MediumGray
                            )
                        }
                        Text(
                            text = "12:00",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeoColors.PureBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(NeoSpacing.md))

                // Evening Reminder Card
                NeoCardFlat(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NeoColors.PureWhite,
                    cornerRadius = NeoDimens.cornerRadius
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(NeoSpacing.lg),
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
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = NeoColors.PureWhite,
                                modifier = Modifier.size(NeoDimens.iconSizeMedium)
                            )
                        }
                        Spacer(modifier = Modifier.width(NeoSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.evening_reminder),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = NeoColors.PureBlack
                            )
                            Text(
                                text = stringResource(R.string.evening_reminder_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = NeoColors.MediumGray
                            )
                        }
                        Text(
                            text = "22:00",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeoColors.PureBlack
                        )
                    }
                }
            }

            // Debug Section - Only visible in debug builds
            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(NeoSpacing.xl))

                Text(
                    text = "Debug Tools",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NeoColors.MediumGray
                )

                Spacer(modifier = Modifier.height(NeoSpacing.md))

                NeoCardFlat(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NeoColors.PureWhite,
                    cornerRadius = NeoDimens.cornerRadius
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(NeoSpacing.lg)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                                    .background(NeoColors.ExpenseRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    tint = NeoColors.PureWhite,
                                    modifier = Modifier.size(NeoDimens.iconSizeMedium)
                                )
                            }
                            Spacer(modifier = Modifier.width(NeoSpacing.md))
                            Column {
                                Text(
                                    text = "Test Notification",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeoColors.PureBlack
                                )
                                Text(
                                    text = "Send test notification to verify it works",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeoColors.MediumGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(NeoSpacing.md))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)
                        ) {
                            NeoButtonText(
                                text = "Lunch (12:00)",
                                onClick = { viewModel.showTestNotification(isLunchTime = true) },
                                modifier = Modifier.weight(1f),
                                backgroundColor = NeoColors.SunYellow,
                                contentColor = NeoColors.PureBlack
                            )
                            NeoButtonText(
                                text = "Evening (22:00)",
                                onClick = { viewModel.showTestNotification(isLunchTime = false) },
                                modifier = Modifier.weight(1f),
                                backgroundColor = NeoColors.DeepPurple,
                                contentColor = NeoColors.PureWhite
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(NeoSpacing.xxl))
        }
    }
}
