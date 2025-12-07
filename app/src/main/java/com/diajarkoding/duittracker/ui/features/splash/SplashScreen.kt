package com.diajarkoding.duittracker.ui.features.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import com.diajarkoding.duittracker.BuildConfig
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var animationStarted by remember { mutableStateOf(false) }
    
    val iconScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.8f,
        animationSpec = tween(durationMillis = 400),
        label = "iconScale"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 150),
        label = "contentAlpha"
    )

    LaunchedEffect(Unit) {
        delay(50)
        animationStarted = true
        
        viewModel.events.collectLatest { event ->
            when (event) {
                is SplashEvent.NavigateToLogin -> onNavigateToLogin()
                is SplashEvent.NavigateToDashboard -> onNavigateToDashboard()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoColors.OffWhite)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Icon - Black circle with yellow dollar
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
            ) {
                // Shadow
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(NeoColors.LightGray)
                )
                // Main circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(NeoColors.PureBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = NeoColors.SunYellow
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(NeoSpacing.xxl))
            
            // App Name
            Text(
                text = "DuitTracker",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = NeoColors.PureBlack,
                modifier = Modifier.graphicsLayer { alpha = contentAlpha }
            )
            
            Spacer(modifier = Modifier.height(NeoSpacing.sm))
            
            // Tagline
            Text(
                text = "Track your money, own your future",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = NeoColors.MediumGray,
                modifier = Modifier.graphicsLayer { alpha = contentAlpha }
            )

            Spacer(modifier = Modifier.height(NeoSpacing.xxxl + NeoSpacing.lg))

            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { alpha = contentAlpha },
                    color = NeoColors.PureBlack,
                    strokeWidth = 3.dp
                )
            }
        }

        // Version at bottom
        Text(
            text = "Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = NeoColors.MediumGray,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = NeoSpacing.xxl)
                .graphicsLayer { alpha = contentAlpha }
        )
    }
}
