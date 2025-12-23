 package com.diajarkoding.duittracker.ui.components
 
 import androidx.compose.foundation.background
 import androidx.compose.foundation.border
 import androidx.compose.foundation.clickable
 import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.size
 import androidx.compose.foundation.shape.CircleShape
 import androidx.compose.material3.MaterialTheme
 import androidx.compose.material3.Text
 import androidx.compose.runtime.Composable
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.draw.clip
 import androidx.compose.ui.graphics.Color
 import androidx.compose.ui.layout.ContentScale
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.unit.Dp
 import androidx.compose.ui.unit.dp
 import coil.compose.AsyncImage
 import com.diajarkoding.duittracker.ui.theme.NeoColors
 import com.diajarkoding.duittracker.ui.theme.NeoDimens
 
 @Composable
 fun NeoAvatar(
     userName: String,
     modifier: Modifier = Modifier,
     avatarUrl: String? = null,
     size: Dp = 44.dp,
     backgroundColor: Color = NeoColors.SunYellow,
     borderColor: Color = NeoColors.PureBlack,
     textColor: Color = NeoColors.PureBlack,
     onClick: (() -> Unit)? = null
 ) {
     val initials = userName
         .split(" ")
         .take(2)
         .mapNotNull { it.firstOrNull()?.uppercaseChar() }
         .joinToString("")
         .ifEmpty { "?" }
 
     Box(
         modifier = modifier
             .size(size)
             .clip(CircleShape)
             .background(backgroundColor)
             .border(NeoDimens.borderWidth, borderColor, CircleShape)
             .then(
                 if (onClick != null) Modifier.clickable(onClick = onClick)
                 else Modifier
             ),
         contentAlignment = Alignment.Center
     ) {
         if (!avatarUrl.isNullOrBlank()) {
             AsyncImage(
                 model = avatarUrl,
                 contentDescription = "Profile picture",
                 modifier = Modifier
                     .size(size)
                     .clip(CircleShape),
                 contentScale = ContentScale.Crop
             )
         } else {
             Text(
                 text = initials,
                 style = MaterialTheme.typography.titleMedium,
                 fontWeight = FontWeight.Bold,
                 color = textColor
             )
         }
     }
 }
