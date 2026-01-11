package com.diajarkoding.duittracker.ui.features.language

import android.app.Activity
import android.os.Build
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diajarkoding.duittracker.R
import com.diajarkoding.duittracker.data.local.preferences.AppLanguage
import com.diajarkoding.duittracker.ui.components.NeoCard
import com.diajarkoding.duittracker.ui.components.NeoCardFlat
import com.diajarkoding.duittracker.ui.components.NeoIconButton
import com.diajarkoding.duittracker.ui.features.profile.ProfileEvent
import com.diajarkoding.duittracker.ui.features.profile.ProfileViewModel
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LanguageScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Listen for LanguageChanged event to recreate activity
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileEvent.LanguageChanged -> {
                    (context as? Activity)?.let { activity ->
                        activity.recreate()
                        @Suppress("DEPRECATION")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            activity.overrideActivityTransition(
                                Activity.OVERRIDE_TRANSITION_OPEN,
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            )
                        } else {
                            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
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
                    text = stringResource(R.string.language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.PureBlack
                )
            }
        },
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
                backgroundColor = NeoColors.DeepPurple,
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
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = NeoColors.PureWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(NeoSpacing.lg))
                    Column {
                        Text(
                            text = stringResource(R.string.language_settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeoColors.PureWhite
                        )
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        Text(
                            text = stringResource(R.string.language_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeoColors.PureWhite.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(NeoSpacing.xl))

            Text(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = NeoColors.MediumGray
            )

            Spacer(modifier = Modifier.height(NeoSpacing.md))

            // Language Options
            AppLanguage.entries.forEach { language ->
                val isSelected = language == uiState.selectedLanguage
                
                NeoCardFlat(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isSelected) {
                                viewModel.setLanguage(language)
                            }
                        },
                    backgroundColor = if (isSelected) NeoColors.DeepPurple else NeoColors.PureWhite,
                    cornerRadius = NeoDimens.cornerRadius
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(NeoSpacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = language.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) NeoColors.PureWhite else NeoColors.PureBlack
                            )
                            Text(
                                text = if (language == AppLanguage.ENGLISH) "English" else "Bahasa Indonesia",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) NeoColors.PureWhite.copy(alpha = 0.8f) else NeoColors.MediumGray
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(NeoColors.PureWhite),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeoColors.DeepPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(NeoSpacing.md))
            }

            Spacer(modifier = Modifier.height(NeoSpacing.xxl))
        }
    }
}
