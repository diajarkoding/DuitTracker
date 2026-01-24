 package com.diajarkoding.duittracker.ui.components
 
 import androidx.compose.foundation.background
 import androidx.compose.foundation.border
 import androidx.compose.foundation.clickable
 import androidx.compose.foundation.layout.Arrangement
 import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.Row
 import androidx.compose.foundation.layout.Spacer
 import androidx.compose.foundation.layout.aspectRatio
 import androidx.compose.foundation.layout.fillMaxWidth
 import androidx.compose.foundation.layout.height
 import androidx.compose.foundation.layout.padding
 import androidx.compose.foundation.layout.size
 import androidx.compose.foundation.layout.width
 import androidx.compose.foundation.lazy.grid.GridCells
 import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
 import androidx.compose.foundation.lazy.grid.items
 import androidx.compose.foundation.shape.RoundedCornerShape
 import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.filled.CalendarMonth
 import androidx.compose.material.icons.filled.ChevronLeft
 import androidx.compose.material.icons.filled.ChevronRight
 import androidx.compose.material3.Icon
 import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.text.style.TextAlign
 import androidx.compose.ui.unit.dp
 import androidx.compose.ui.window.Dialog
import com.diajarkoding.duittracker.R
 import com.diajarkoding.duittracker.ui.theme.NeoColors
import com.diajarkoding.duittracker.ui.theme.NeoTheme
 import com.diajarkoding.duittracker.ui.theme.NeoDimens
 import com.diajarkoding.duittracker.ui.theme.NeoSpacing
 import kotlinx.datetime.Clock
 import kotlinx.datetime.DateTimeUnit
 import kotlinx.datetime.LocalDate
 import kotlinx.datetime.TimeZone
 import kotlinx.datetime.minus
 import kotlinx.datetime.plus
 import kotlinx.datetime.toLocalDateTime

// Simple NeoDatePickerField - just label and date text
@Composable
fun NeoDatePickerField(
    selectedDate: LocalDate,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date"
) {
    Column(
        modifier = modifier.clickable { onDateClick() },
        verticalArrangement = Arrangement.spacedBy(NeoSpacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = NeoTheme.colors.textSecondary
        )
        Text(
            text = formatDateLocalized(selectedDate),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = NeoTheme.colors.textPrimary
        )
    }
}

/*
 * Old NeoDatePickerField with card and icon - kept for reference
 *
@Composable
fun NeoDatePickerFieldWithCard(
    selectedDate: LocalDate,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date"
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = NeoTheme.colors.textSecondary
        )
        NeoCardFlat(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDateClick() },
            backgroundColor = NeoTheme.colors.cardBackground,
            cornerRadius = NeoDimens.cornerRadiusSmall,
            borderWidth = NeoDimens.borderWidth
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NeoSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                            .background(NeoColors.ElectricBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = NeoTheme.colors.cardBackground,
                            modifier = Modifier.size(NeoDimens.iconSizeSmall)
                        )
                    }
                    Text(
                        text = formatDateLocalized(selectedDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = NeoTheme.colors.textPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.select_date),
                    tint = NeoTheme.colors.textSecondary,
                    modifier = Modifier.size(NeoDimens.iconSizeMedium)
                )
            }
        }
    }
}
*/

 @Composable
 fun NeoDatePickerDialog(
     initialDate: LocalDate,
     onDateSelected: (LocalDate) -> Unit,
     onDismiss: () -> Unit
 ) {
     var currentMonth by remember { mutableStateOf(initialDate) }
     var selectedDate by remember { mutableStateOf(initialDate) }
     val today = remember {
         Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
     }
 
     Dialog(onDismissRequest = onDismiss) {
         NeoCard(
             modifier = Modifier.fillMaxWidth(),
             backgroundColor = NeoTheme.colors.cardBackground,
             shadowOffset = NeoDimens.shadowOffset,
             cornerRadius = NeoDimens.cornerRadius
         ) {
             Column(
                 modifier = Modifier.padding(NeoSpacing.lg)
             ) {
                 // Header
                 Text(
                    text = stringResource(R.string.select_date),
                     style = MaterialTheme.typography.titleLarge,
                     fontWeight = FontWeight.Bold,
                     color = NeoTheme.colors.textPrimary
                 )
 
                 Spacer(modifier = Modifier.height(NeoSpacing.lg))
 
                 // Month navigation
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.SpaceBetween,
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     IconButton(
                         onClick = {
                             currentMonth = currentMonth.minus(1, DateTimeUnit.MONTH)
                         }
                     ) {
                         Box(
                             modifier = Modifier
                                 .size(36.dp)
                                 .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                                 .background(NeoTheme.colors.lightGray)
                                 .border(
                                     NeoDimens.borderWidth,
                                     NeoTheme.colors.textPrimary,
                                     RoundedCornerShape(NeoDimens.cornerRadiusSmall)
                                 ),
                             contentAlignment = Alignment.Center
                         ) {
                             Icon(
                                 imageVector = Icons.Default.ChevronLeft,
                                contentDescription = stringResource(R.string.previous_month),
                                 tint = NeoTheme.colors.textPrimary
                             )
                         }
                     }
 
                     Text(
                        text = getMonthYearStringLocalized(currentMonth),
                         style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.Bold,
                         color = NeoTheme.colors.textPrimary
                     )
 
                     IconButton(
                         onClick = {
                             currentMonth = currentMonth.plus(1, DateTimeUnit.MONTH)
                         }
                     ) {
                         Box(
                             modifier = Modifier
                                 .size(36.dp)
                                 .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                                 .background(NeoTheme.colors.lightGray)
                                 .border(
                                     NeoDimens.borderWidth,
                                     NeoTheme.colors.textPrimary,
                                     RoundedCornerShape(NeoDimens.cornerRadiusSmall)
                                 ),
                             contentAlignment = Alignment.Center
                         ) {
                             Icon(
                                 imageVector = Icons.Default.ChevronRight,
                                contentDescription = stringResource(R.string.next_month),
                                 tint = NeoTheme.colors.textPrimary
                             )
                         }
                     }
                 }
 
                 Spacer(modifier = Modifier.height(NeoSpacing.md))
 
                 // Day of week headers
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.SpaceEvenly
                 ) {
                    listOf(
                        stringResource(R.string.day_sun),
                        stringResource(R.string.day_mon),
                        stringResource(R.string.day_tue),
                        stringResource(R.string.day_wed),
                        stringResource(R.string.day_thu),
                        stringResource(R.string.day_fri),
                        stringResource(R.string.day_sat)
                    ).forEach { day ->
                         Text(
                             text = day,
                             style = MaterialTheme.typography.labelSmall,
                             fontWeight = FontWeight.Bold,
                             color = NeoTheme.colors.textSecondary,
                             textAlign = TextAlign.Center,
                             modifier = Modifier.weight(1f)
                         )
                     }
                 }
 
                 Spacer(modifier = Modifier.height(NeoSpacing.sm))
 
                 // Calendar grid
                 val calendarDays = getCalendarDays(currentMonth)
                 LazyVerticalGrid(
                     columns = GridCells.Fixed(7),
                     modifier = Modifier.height(240.dp),
                     horizontalArrangement = Arrangement.spacedBy(4.dp),
                     verticalArrangement = Arrangement.spacedBy(4.dp)
                 ) {
                     items(calendarDays) { day ->
                         if (day != null) {
                             val isSelected = day == selectedDate
                             val isToday = day == today
                             val isCurrentMonth = day.month == currentMonth.month
 
                             Box(
                                 modifier = Modifier
                                     .aspectRatio(1f)
                                     .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                                     .background(
                                         when {
                                             isSelected -> NeoColors.SunYellow
                                             isToday -> NeoColors.ElectricBlue.copy(alpha = 0.2f)
                                             else -> NeoTheme.colors.cardBackground
                                         }
                                     )
                                     .then(
                                         if (isSelected) {
                                             Modifier.border(
                                                 NeoDimens.borderWidth,
                                                 NeoTheme.colors.textPrimary,
                                                 RoundedCornerShape(NeoDimens.cornerRadiusSmall)
                                             )
                                         } else Modifier
                                     )
                                     .clickable { selectedDate = day },
                                 contentAlignment = Alignment.Center
                             ) {
                                 Text(
                                     text = day.dayOfMonth.toString(),
                                     style = MaterialTheme.typography.bodyMedium,
                                     fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                     color = when {
                                         isSelected -> NeoTheme.colors.textPrimary
                                         !isCurrentMonth -> NeoTheme.colors.lightGray
                                         isToday -> NeoColors.ElectricBlue
                                         else -> NeoTheme.colors.textPrimary
                                     }
                                 )
                             }
                         } else {
                             Box(modifier = Modifier.aspectRatio(1f))
                         }
                     }
                 }
 
                 Spacer(modifier = Modifier.height(NeoSpacing.lg))
 
                 // Buttons
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md)
                 ) {
                     NeoButtonText(
                        text = stringResource(R.string.cancel),
                         onClick = onDismiss,
                         modifier = Modifier.weight(1f),
                         backgroundColor = NeoTheme.colors.lightGray,
                         contentColor = NeoTheme.colors.textPrimary
                     )
                     NeoButtonText(
                        text = stringResource(R.string.confirm),
                         onClick = { onDateSelected(selectedDate) },
                         modifier = Modifier.weight(1f),
                         backgroundColor = NeoTheme.colors.textPrimary,
                         contentColor = NeoColors.SunYellow
                     )
                 }
             }
         }
     }
 }
 
@Composable
private fun formatDateLocalized(date: LocalDate): String {
    val dayOfWeek = when (date.dayOfWeek.ordinal) {
        0 -> stringResource(R.string.day_monday)
        1 -> stringResource(R.string.day_tuesday)
        2 -> stringResource(R.string.day_wednesday)
        3 -> stringResource(R.string.day_thursday)
        4 -> stringResource(R.string.day_friday)
        5 -> stringResource(R.string.day_saturday)
        6 -> stringResource(R.string.day_sunday)
        else -> ""
     }
    val month = getMonthNameLocalized(date.monthNumber)
     return "$dayOfWeek, ${date.dayOfMonth} $month ${date.year}"
 }
 
@Composable
private fun getMonthNameLocalized(month: Int): String {
     return when (month) {
        1 -> stringResource(R.string.month_january)
        2 -> stringResource(R.string.month_february)
        3 -> stringResource(R.string.month_march)
        4 -> stringResource(R.string.month_april)
        5 -> stringResource(R.string.month_may)
        6 -> stringResource(R.string.month_june)
        7 -> stringResource(R.string.month_july)
        8 -> stringResource(R.string.month_august)
        9 -> stringResource(R.string.month_september)
        10 -> stringResource(R.string.month_october)
        11 -> stringResource(R.string.month_november)
        12 -> stringResource(R.string.month_december)
         else -> ""
     }
 }
 
@Composable
private fun getMonthYearStringLocalized(date: LocalDate): String {
    return "${getMonthNameLocalized(date.monthNumber)} ${date.year}"
 }
 
 private fun getCalendarDays(currentMonth: LocalDate): List<LocalDate?> {
     val firstDayOfMonth = LocalDate(currentMonth.year, currentMonth.month, 1)
     val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal
     val daysInMonth = when (currentMonth.monthNumber) {
         1, 3, 5, 7, 8, 10, 12 -> 31
         4, 6, 9, 11 -> 30
         2 -> if (isLeapYear(currentMonth.year)) 29 else 28
         else -> 30
     }
 
     val days = mutableListOf<LocalDate?>()
 
     // Adjust for Sunday = 0 (kotlinx.datetime uses Monday = 0)
     val startOffset = (firstDayOfWeek + 1) % 7
 
     // Add empty days before the first day
     repeat(startOffset) {
         days.add(null)
     }
 
     // Add days of the month
     for (day in 1..daysInMonth) {
         days.add(LocalDate(currentMonth.year, currentMonth.month, day))
     }
 
     // Add remaining empty days to complete the grid
     while (days.size % 7 != 0) {
         days.add(null)
     }
 
     return days
 }
 
 private fun isLeapYear(year: Int): Boolean {
     return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
 }
