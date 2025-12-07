package com.diajarkoding.duittracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diajarkoding.duittracker.ui.theme.NeoColors

@Composable
fun NeoToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = NeoColors.ElectricBlue,
    unselectedColor: Color = NeoColors.PureWhite,
    borderColor: Color = NeoColors.PureBlack,
    shadowColor: Color = NeoColors.PureBlack,
    borderWidth: Dp = 2.dp,
    cornerRadius: Dp = 4.dp,
    shadowOffset: Dp = 4.dp
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(modifier = modifier) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .clip(shape)
                .background(shadowColor)
        )

        // Toggle container
        Row(
            modifier = Modifier
                .clip(shape)
                .background(unselectedColor)
                .border(borderWidth, borderColor, shape),
            horizontalArrangement = Arrangement.Center
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) selectedColor else unselectedColor,
                    label = "toggleBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) NeoColors.PureWhite else NeoColors.PureBlack,
                    label = "toggleText"
                )

                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(backgroundColor)
                        .clickable { onSelectionChange(index) }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun NeoExpenseIncomeToggle(
    isExpense: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val expenseColor = if (isExpense) NeoColors.ExpenseRed else NeoColors.PureWhite
    val incomeColor = if (!isExpense) NeoColors.IncomeGreen else NeoColors.PureWhite

    Box(modifier = modifier) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(NeoColors.PureBlack)
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(NeoColors.PureWhite)
                .border(2.dp, NeoColors.PureBlack, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(expenseColor)
                    .clickable { onToggle(true) }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EXPENSE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) NeoColors.PureWhite else NeoColors.PureBlack
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(incomeColor)
                    .clickable { onToggle(false) }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "INCOME",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (!isExpense) NeoColors.PureWhite else NeoColors.PureBlack
                )
            }
        }
    }
}
