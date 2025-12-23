 package com.diajarkoding.duittracker.ui.components
 
 import androidx.compose.foundation.background
 import androidx.compose.foundation.border
 import androidx.compose.foundation.clickable
 import androidx.compose.foundation.layout.Arrangement
 import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.Row
 import androidx.compose.foundation.layout.Spacer
 import androidx.compose.foundation.layout.fillMaxWidth
 import androidx.compose.foundation.layout.height
 import androidx.compose.foundation.layout.padding
 import androidx.compose.foundation.layout.size
 import androidx.compose.foundation.layout.width
 import androidx.compose.foundation.lazy.LazyColumn
 import androidx.compose.foundation.lazy.rememberLazyListState
 import androidx.compose.foundation.shape.RoundedCornerShape
 import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.filled.KeyboardArrowDown
 import androidx.compose.material.icons.filled.KeyboardArrowUp
 import androidx.compose.material3.Icon
 import androidx.compose.material3.IconButton
 import androidx.compose.material3.MaterialTheme
 import androidx.compose.material3.Text
 import androidx.compose.runtime.Composable
 import androidx.compose.runtime.LaunchedEffect
 import androidx.compose.runtime.getValue
 import androidx.compose.runtime.mutableIntStateOf
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
 
 @Composable
 fun NeoTimePickerDialog(
     initialHour: Int,
     initialMinute: Int,
     onTimeSelected: (hour: Int, minute: Int) -> Unit,
     onDismiss: () -> Unit
 ) {
     var selectedHour by remember { mutableIntStateOf(initialHour) }
     var selectedMinute by remember { mutableIntStateOf(initialMinute) }
 
     Dialog(onDismissRequest = onDismiss) {
         NeoCard(
             modifier = Modifier.fillMaxWidth(),
             backgroundColor = NeoColors.PureWhite,
             shadowOffset = NeoDimens.shadowOffset,
             cornerRadius = NeoDimens.cornerRadius
         ) {
             Column(
                 modifier = Modifier.padding(NeoSpacing.xl),
                 horizontalAlignment = Alignment.CenterHorizontally
             ) {
                 Text(
                     text = "Set Reminder Time",
                     style = MaterialTheme.typography.titleLarge,
                     fontWeight = FontWeight.Bold,
                     color = NeoColors.PureBlack
                 )
 
                 Spacer(modifier = Modifier.height(NeoSpacing.xl))
 
                 Row(
                     horizontalArrangement = Arrangement.Center,
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     // Hour Picker
                     NumberPicker(
                         value = selectedHour,
                         range = 0..23,
                         onValueChange = { selectedHour = it }
                     )
 
                     Text(
                         text = ":",
                         style = MaterialTheme.typography.headlineLarge,
                         fontWeight = FontWeight.Bold,
                         color = NeoColors.PureBlack,
                         modifier = Modifier.padding(horizontal = NeoSpacing.md)
                     )
 
                     // Minute Picker
                     NumberPicker(
                         value = selectedMinute,
                         range = 0..59,
                         onValueChange = { selectedMinute = it }
                     )
                 }
 
                 Spacer(modifier = Modifier.height(NeoSpacing.xl))
 
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
                         onClick = { onTimeSelected(selectedHour, selectedMinute) },
                         modifier = Modifier.weight(1f),
                         backgroundColor = NeoColors.PureBlack,
                         contentColor = NeoColors.SunYellow
                     )
                 }
             }
         }
     }
 }
 
 @Composable
 private fun NumberPicker(
     value: Int,
     range: IntRange,
     onValueChange: (Int) -> Unit
 ) {
     Column(
         horizontalAlignment = Alignment.CenterHorizontally
     ) {
         IconButton(
             onClick = {
                 val newValue = if (value >= range.last) range.first else value + 1
                 onValueChange(newValue)
             }
         ) {
             Icon(
                 imageVector = Icons.Default.KeyboardArrowUp,
                 contentDescription = "Increase",
                 tint = NeoColors.PureBlack,
                 modifier = Modifier.size(NeoDimens.iconSizeLarge)
             )
         }
 
         Box(
             modifier = Modifier
                 .size(72.dp, 56.dp)
                 .clip(RoundedCornerShape(NeoDimens.cornerRadiusSmall))
                 .background(NeoColors.SunYellow)
                 .border(
                     NeoDimens.borderWidth,
                     NeoColors.PureBlack,
                     RoundedCornerShape(NeoDimens.cornerRadiusSmall)
                 ),
             contentAlignment = Alignment.Center
         ) {
             Text(
                 text = String.format("%02d", value),
                 style = MaterialTheme.typography.headlineMedium,
                 fontWeight = FontWeight.Bold,
                 color = NeoColors.PureBlack,
                 textAlign = TextAlign.Center
             )
         }
 
         IconButton(
             onClick = {
                 val newValue = if (value <= range.first) range.last else value - 1
                 onValueChange(newValue)
             }
         ) {
             Icon(
                 imageVector = Icons.Default.KeyboardArrowDown,
                 contentDescription = "Decrease",
                 tint = NeoColors.PureBlack,
                 modifier = Modifier.size(NeoDimens.iconSizeLarge)
             )
         }
     }
 }
