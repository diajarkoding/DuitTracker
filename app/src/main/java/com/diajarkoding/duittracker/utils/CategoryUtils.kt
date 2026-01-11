package com.diajarkoding.duittracker.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChairAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.diajarkoding.duittracker.R
import com.diajarkoding.duittracker.data.model.TransactionCategory
import com.diajarkoding.duittracker.ui.theme.NeoColors

object CategoryUtils {

    fun getIcon(category: TransactionCategory): ImageVector {
        return when (category) {
            TransactionCategory.FOOD -> Icons.Default.Restaurant
            TransactionCategory.TRANSPORT -> Icons.Default.DirectionsCar
            TransactionCategory.SHOPPING -> Icons.Default.ShoppingBag
            TransactionCategory.ENTERTAINMENT -> Icons.Default.Movie
            TransactionCategory.BILLS -> Icons.Default.Receipt
            TransactionCategory.HEALTH -> Icons.Default.FitnessCenter
            TransactionCategory.EDUCATION -> Icons.Default.School
            TransactionCategory.SOCIAL -> Icons.Default.Face
            TransactionCategory.SALARY -> Icons.Default.Payments
            TransactionCategory.INVESTMENT -> Icons.Default.AttachMoney
            TransactionCategory.DAILY_NEEDS -> Icons.Default.ChairAlt
            TransactionCategory.GIFT -> Icons.Default.CardGiftcard
            TransactionCategory.OTHER -> Icons.Default.MoreHoriz
        }
    }

    fun getColor(category: TransactionCategory): Color {
        return when (category) {
            TransactionCategory.FOOD -> NeoColors.FoodOrange
            TransactionCategory.TRANSPORT -> NeoColors.TransportBlue
            TransactionCategory.SHOPPING -> NeoColors.ShoppingPink
            TransactionCategory.ENTERTAINMENT -> NeoColors.EntertainmentPurple
            TransactionCategory.BILLS -> NeoColors.BillsRed
            TransactionCategory.HEALTH -> NeoColors.HealthGreen
            TransactionCategory.EDUCATION -> NeoColors.EducationYellow
            TransactionCategory.SOCIAL -> NeoColors.HotPink
            TransactionCategory.SALARY -> NeoColors.IncomeGreen
            TransactionCategory.INVESTMENT -> NeoColors.DeepPurple
            TransactionCategory.DAILY_NEEDS -> NeoColors.DailyNeedsBlue
            TransactionCategory.GIFT -> NeoColors.GiftMagenta
            TransactionCategory.OTHER -> NeoColors.OtherGray
        }
    }

    fun getDisplayName(category: TransactionCategory): String {
        return when (category) {
            TransactionCategory.FOOD -> "Food"
            TransactionCategory.TRANSPORT -> "Transport"
            TransactionCategory.SHOPPING -> "Shopping"
            TransactionCategory.ENTERTAINMENT -> "Entertainment"
            TransactionCategory.BILLS -> "Bills"
            TransactionCategory.HEALTH -> "Health"
            TransactionCategory.EDUCATION -> "Education"
            TransactionCategory.SOCIAL -> "Social"
            TransactionCategory.SALARY -> "Salary"
            TransactionCategory.INVESTMENT -> "Investment"
            TransactionCategory.DAILY_NEEDS -> "Daily Needs"
            TransactionCategory.GIFT -> "Gift"
            TransactionCategory.OTHER -> "Other"
        }
    }

    @StringRes
    fun getDisplayNameResId(category: TransactionCategory): Int {
        return when (category) {
            TransactionCategory.FOOD -> R.string.category_food
            TransactionCategory.TRANSPORT -> R.string.category_transport
            TransactionCategory.SHOPPING -> R.string.category_shopping
            TransactionCategory.ENTERTAINMENT -> R.string.category_entertainment
            TransactionCategory.BILLS -> R.string.category_bills
            TransactionCategory.HEALTH -> R.string.category_health
            TransactionCategory.EDUCATION -> R.string.category_education
            TransactionCategory.SOCIAL -> R.string.category_social
            TransactionCategory.SALARY -> R.string.category_salary
            TransactionCategory.INVESTMENT -> R.string.category_investment
            TransactionCategory.DAILY_NEEDS -> R.string.category_daily_needs
            TransactionCategory.GIFT -> R.string.category_gift
            TransactionCategory.OTHER -> R.string.category_other
        }
    }

    @Composable
    fun getLocalizedDisplayName(category: TransactionCategory): String {
        return stringResource(getDisplayNameResId(category))
    }
}
