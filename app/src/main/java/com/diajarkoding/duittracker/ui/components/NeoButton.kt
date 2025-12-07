package com.diajarkoding.duittracker.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diajarkoding.duittracker.ui.theme.NeoColors

@Composable
fun NeoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = NeoColors.ElectricBlue,
    contentColor: Color = NeoColors.PureWhite,
    borderColor: Color = NeoColors.PureBlack,
    shadowColor: Color = NeoColors.PureBlack,
    borderWidth: Dp = 2.dp,
    cornerRadius: Dp = 4.dp,
    shadowOffset: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedOffset by animateDpAsState(
        targetValue = if (isPressed) shadowOffset else 0.dp,
        label = "buttonOffset"
    )
    val animatedShadowOffset by animateDpAsState(
        targetValue = if (isPressed) 0.dp else shadowOffset,
        label = "shadowOffset"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val actualBackgroundColor = if (enabled) backgroundColor else NeoColors.OtherGray

    Box(modifier = modifier) {
        // Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = animatedShadowOffset, y = animatedShadowOffset)
                .clip(shape)
                .background(shadowColor)
        )

        // Button
        Box(
            modifier = Modifier
                .offset(x = animatedOffset, y = animatedOffset)
                .clip(shape)
                .background(actualBackgroundColor)
                .border(borderWidth, borderColor, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

@Composable
fun NeoButtonText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = NeoColors.ElectricBlue,
    contentColor: Color = NeoColors.PureWhite
) {
    NeoButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        backgroundColor = backgroundColor,
        contentColor = contentColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NeoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = NeoColors.PureWhite,
    contentColor: Color = NeoColors.PureBlack,
    size: Dp = 48.dp,
    content: @Composable () -> Unit
) {
    NeoButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentPadding = PaddingValues(12.dp)
    ) {
        content()
    }
}
