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
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.text.style.TextAlign
 import androidx.compose.ui.unit.dp
 import androidx.compose.ui.window.Dialog
 import com.diajarkoding.duittracker.ui.theme.NeoColors
 import com.diajarkoding.duittracker.ui.theme.NeoDimens
 import com.diajarkoding.duittracker.ui.theme.NeoSpacing
 import kotlinx.datetime.Clock
 import kotlinx.datetime.DateTimeUnit
 import kotlinx.datetime.LocalDate
 import kotlinx.datetime.TimeZone
 import kotlinx.datetime.minus
 import kotlinx.datetime.plus
 import kotlinx.datetime.toLocalDateTime
 
 @Composable
 fun NeoDatePickerField(
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
             color = NeoColors.MediumGray
         )
         NeoCardFlat(
             modifier = Modifier
                 .fillMaxWidth()
                 .clickable { onDateClick() },
             backgroundColor = NeoColors.PureWhite,
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
                             tint = NeoColors.PureWhite,
                             modifier = Modifier.size(NeoDimens.iconSizeSmall)
                         )
                     }
                     Text(
                         text = formatDate(selectedDate),
                         style = MaterialTheme.typography.bodyMedium,
                         fontWeight = FontWeight.Medium,
                         color = NeoColors.PureBlack
                     )
                 }
                 Icon(
                     imageVector = Icons.Default.ChevronRight,
                     contentDescription = "Select date",
                     tint = NeoColors.MediumGray,
                     modifier = Modifier.size(NeoDimens.iconSizeMedium)
                 )
             }
         }
     }
 }
 
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
             backgroundColor = NeoColors.PureWhite,
             shadowOffset = NeoDimens.shadowOffset,
             cornerRadius = NeoDimens.cornerRadius
         ) {
             Column(
                 modifier = Modifier.padding(NeoSpacing.lg)
             ) {
                 // Header
                 Text(
                     text = "Select Date",
                     style = MaterialTheme.typography.titleLarge,
                     fontWeight = FontWeight.Bold,
                     color = NeoColors.PureBlack
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
                                 .background(NeoColors.LightGray)
                                 .border(
                                     NeoDimens.borderWidth,
                                     NeoColors.PureBlack,
                                     RoundedCornerShape(NeoDimens.cornerRadiusSmall)
                                 ),
                             contentAlignment = Alignment.Center
                         ) {
                             Icon(
                                 imageVector = Icons.Default.ChevronLeft,
                                 contentDescription = "Previous month",
                                 tint = NeoColors.PureBlack
                             )
                         }
                     }
 
                     Text(
                         text = getMonthYearString(currentMonth),
                         style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.Bold,
                         color = NeoColors.PureBlack
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
                                 .background(NeoColors.LightGray)
                                 .border(
                                     NeoDimens.borderWidth,
                                     NeoColors.PureBlack,
                                     RoundedCornerShape(NeoDimens.cornerRadiusSmall)
                                 ),
                             contentAlignment = Alignment.Center
                         ) {
                             Icon(
                                 imageVector = Icons.Default.ChevronRight,
                                 contentDescription = "Next month",
                                 tint = NeoColors.PureBlack
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
                     listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                         Text(
                             text = day,
                             style = MaterialTheme.typography.labelSmall,
                             fontWeight = FontWeight.Bold,
                             color = NeoColors.MediumGray,
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
                                             else -> NeoColors.PureWhite
                                         }
                                     )
                                     .then(
                                         if (isSelected) {
                                             Modifier.border(
                                                 NeoDimens.borderWidth,
                                                 NeoColors.PureBlack,
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
                                         isSelected -> NeoColors.PureBlack
                                         !isCurrentMonth -> NeoColors.LightGray
                                         isToday -> NeoColors.ElectricBlue
                                         else -> NeoColors.PureBlack
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
                         text = "Cancel",
                         onClick = onDismiss,
                         modifier = Modifier.weight(1f),
                         backgroundColor = NeoColors.LightGray,
                         contentColor = NeoColors.PureBlack
                     )
                     NeoButtonText(
                         text = "Confirm",
                         onClick = { onDateSelected(selectedDate) },
                         modifier = Modifier.weight(1f),
                         backgroundColor = NeoColors.PureBlack,
                         contentColor = NeoColors.SunYellow
                     )
                 }
             }
         }
     }
 }
 
 private fun formatDate(date: LocalDate): String {
     val dayOfWeek = when (date.dayOfWeek.ordinal) {
         0 -> "Monday"
         1 -> "Tuesday"
         2 -> "Wednesday"
         3 -> "Thursday"
         4 -> "Friday"
         5 -> "Saturday"
         6 -> "Sunday"
         else -> ""
     }
     val month = getMonthName(date.monthNumber)
     return "$dayOfWeek, ${date.dayOfMonth} $month ${date.year}"
 }
 
 private fun getMonthName(month: Int): String {
     return when (month) {
         1 -> "January"
         2 -> "February"
         3 -> "March"
         4 -> "April"
         5 -> "May"
         6 -> "June"
         7 -> "July"
         8 -> "August"
         9 -> "September"
         10 -> "October"
         11 -> "November"
         12 -> "December"
         else -> ""
     }
 }
 
 private fun getMonthYearString(date: LocalDate): String {
     return "${getMonthName(date.monthNumber)} ${date.year}"
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
