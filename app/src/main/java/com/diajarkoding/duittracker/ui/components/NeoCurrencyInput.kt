package com.diajarkoding.duittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoDimens
import com.diajarkoding.duittracker.ui.theme.NeoSpacing
import java.text.NumberFormat
import java.util.Locale

private val indonesianLocale = Locale.Builder()
    .setLanguage("id")
    .setRegion("ID")
    .build()

private fun formatNumber(value: String): String {
    if (value.isEmpty()) return ""
    val numberFormat = NumberFormat.getNumberInstance(indonesianLocale)
    val numericValue = value.filter { it.isDigit() }.toLongOrNull() ?: 0L
    return numberFormat.format(numericValue)
}

@Composable
fun NeoCurrencyInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "0",
    enabled: Boolean = true,
    backgroundColor: Color = NeoColors.PureWhite,
    borderColor: Color = NeoColors.PureBlack,
    shadowColor: Color = NeoColors.PureBlack,
    borderWidth: Dp = NeoDimens.borderWidth,
    cornerRadius: Dp = NeoDimens.cornerRadius,
    shadowOffset: Dp = NeoDimens.shadowOffset,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    // Format the display value and keep cursor at end
    val formattedValue = formatNumber(value)
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = formattedValue,
                selection = TextRange(formattedValue.length)
            )
        )
    }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = NeoColors.MediumGray
            )
            Spacer(modifier = Modifier.height(NeoSpacing.sm))
        }

        Box {
            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = shadowOffset, y = shadowOffset)
                    .clip(shape)
                    .background(shadowColor)
            )

            // Input field
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newTextFieldValue ->
                    // Extract only digits from the new input
                    val newDigits = newTextFieldValue.text.filter { it.isDigit() }
                    val oldDigits = value
                    
                    // Only update if digits changed
                    if (newDigits != oldDigits) {
                        onValueChange(newDigits)
                        // Format and set cursor to end
                        val formatted = formatNumber(newDigits)
                        textFieldValue = TextFieldValue(
                            text = formatted,
                            selection = TextRange(formatted.length)
                        )
                    } else {
                        // Keep cursor at end even if no change
                        textFieldValue = textFieldValue.copy(
                            selection = TextRange(textFieldValue.text.length)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(backgroundColor)
                    .border(borderWidth, borderColor, shape)
                    .padding(NeoSpacing.lg),
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoColors.PureBlack
                ),
                cursorBrush = SolidColor(NeoColors.ElectricBlue),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = keyboardActions,
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rp",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeoColors.MediumGray
                            ),
                            modifier = Modifier.padding(end = NeoSpacing.sm)
                        )
                        Box {
                            if (textFieldValue.text.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeoColors.LightGray
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun NeoCurrencyInputFlat(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "0",
    enabled: Boolean = true,
    backgroundColor: Color = NeoColors.PureWhite,
    borderColor: Color = NeoColors.PureBlack,
    borderWidth: Dp = NeoDimens.borderWidth,
    cornerRadius: Dp = NeoDimens.cornerRadius,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    val formattedValue = formatNumber(value)
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = formattedValue,
                selection = TextRange(formattedValue.length)
            )
        )
    }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = NeoColors.MediumGray
            )
            Spacer(modifier = Modifier.height(NeoSpacing.sm))
        }

        BasicTextField(
            value = textFieldValue,
            onValueChange = { newTextFieldValue ->
                val newDigits = newTextFieldValue.text.filter { it.isDigit() }
                val oldDigits = value
                
                if (newDigits != oldDigits) {
                    onValueChange(newDigits)
                    val formatted = formatNumber(newDigits)
                    textFieldValue = TextFieldValue(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )
                } else {
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(textFieldValue.text.length)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(backgroundColor)
                .border(borderWidth, borderColor, shape)
                .padding(NeoSpacing.lg),
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NeoColors.PureBlack
            ),
            cursorBrush = SolidColor(NeoColors.ElectricBlue),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = keyboardActions,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rp",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoColors.MediumGray
                        ),
                        modifier = Modifier.padding(end = NeoSpacing.sm)
                    )
                    Box {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeoColors.LightGray
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}
