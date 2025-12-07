package com.diajarkoding.duittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diajarkoding.duittracker.ui.theme.NeoColors

enum class SnackbarType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

object NeoSnackbarColors {
    val SuccessBackground = Color(0xFF00CC66)
    val SuccessContent = Color(0xFFFFFFFF)
    
    val ErrorBackground = Color(0xFFFF3333)
    val ErrorContent = Color(0xFFFFFFFF)
    
    val WarningBackground = Color(0xFFFFCC00)
    val WarningContent = Color(0xFF1A1A1A)
    
    val InfoBackground = Color(0xFF0066FF)
    val InfoContent = Color(0xFFFFFFFF)
}

data class NeoSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: androidx.compose.material3.SnackbarDuration = androidx.compose.material3.SnackbarDuration.Short,
    val type: SnackbarType = SnackbarType.INFO
) : androidx.compose.material3.SnackbarVisuals

@Composable
fun NeoSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            val visuals = snackbarData.visuals
            val type = if (visuals is NeoSnackbarVisuals) visuals.type else SnackbarType.INFO
            NeoSnackbar(
                message = visuals.message,
                type = type
            )
        }
    )
}

@Composable
fun NeoSnackbar(
    message: String,
    type: SnackbarType,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor, icon) = when (type) {
        SnackbarType.SUCCESS -> Triple(
            NeoSnackbarColors.SuccessBackground,
            NeoSnackbarColors.SuccessContent,
            Icons.Default.CheckCircle
        )
        SnackbarType.ERROR -> Triple(
            NeoSnackbarColors.ErrorBackground,
            NeoSnackbarColors.ErrorContent,
            Icons.Default.Error
        )
        SnackbarType.WARNING -> Triple(
            NeoSnackbarColors.WarningBackground,
            NeoSnackbarColors.WarningContent,
            Icons.Default.Warning
        )
        SnackbarType.INFO -> Triple(
            NeoSnackbarColors.InfoBackground,
            NeoSnackbarColors.InfoContent,
            Icons.Default.CheckCircle
        )
    }

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 2.dp,
                color = NeoColors.PureBlack,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

suspend fun SnackbarHostState.showNeoSnackbar(
    message: String,
    type: SnackbarType = SnackbarType.INFO,
    duration: androidx.compose.material3.SnackbarDuration = androidx.compose.material3.SnackbarDuration.Short
) {
    showSnackbar(
        NeoSnackbarVisuals(
            message = message,
            type = type,
            duration = duration
        )
    )
}
