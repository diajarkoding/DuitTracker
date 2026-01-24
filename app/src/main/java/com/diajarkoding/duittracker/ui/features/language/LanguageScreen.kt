package com.diajarkoding.duittracker.ui.features.language

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diajarkoding.duittracker.R
import com.diajarkoding.duittracker.data.local.preferences.AppLanguage
import com.diajarkoding.duittracker.ui.components.NeoCard
import com.diajarkoding.duittracker.ui.components.NeoCardFlat
import com.diajarkoding.duittracker.ui.components.NeoIconButton
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoTheme
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import com.diajarkoding.duittracker.utils.LocaleManager

@Composable
fun LanguageScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Observe configurationVersion to trigger recomposition when language changes
    val configVersion = LocaleManager.configurationVersion
    
    // Get current language - will update when configVersion changes
    val currentLanguage = LocaleManager.getCurrentLanguage()
    
    // Create updated context for string resources
    val updatedContext = remember(configVersion) {
        LocaleManager.attachBaseContext(context)
    }
    
    // Helper function to get string from updated context
    fun getString(resId: Int): String = updatedContext.getString(resId)

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
                        contentDescription = getString(R.string.back),
                        modifier = Modifier.size(NeoDimens.iconSizeMedium)
                    )
                }
                Spacer(modifier = Modifier.width(NeoSpacing.md))
                Text(
                    text = getString(R.string.language),
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
                            text = getString(R.string.language_settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeoColors.PureWhite
                        )
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        Text(
                            text = getString(R.string.language_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeoColors.PureWhite.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(NeoSpacing.xl))

            Text(
                text = getString(R.string.select_language),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = NeoTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(NeoSpacing.md))

            // Language Options
            AppLanguage.entries.forEach { language ->
                val isSelected = language == currentLanguage
                
                NeoCardFlat(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isSelected) {
                                LocaleManager.setLanguage(context, language)
                            }
                        },
                    backgroundColor = if (isSelected) NeoColors.DeepPurple else NeoTheme.colors.cardBackground,
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
                                color = if (isSelected) NeoColors.PureWhite else NeoTheme.colors.textPrimary
                            )
                            Text(
                                text = if (language == AppLanguage.ENGLISH) "English" else "Bahasa Indonesia",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) NeoColors.PureWhite.copy(alpha = 0.8f) else NeoTheme.colors.textSecondary
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
