 package com.diajarkoding.duittracker.widget
 
 import android.content.Context
 import androidx.compose.runtime.Composable
 import androidx.compose.ui.graphics.Color
 import androidx.compose.ui.unit.dp
 import androidx.compose.ui.unit.sp
 import androidx.glance.GlanceId
 import androidx.glance.GlanceModifier
 import androidx.glance.GlanceTheme
 import androidx.glance.action.actionStartActivity
 import androidx.glance.action.clickable
 import androidx.glance.appwidget.GlanceAppWidget
 import androidx.glance.appwidget.provideContent
 import androidx.glance.appwidget.cornerRadius
 import androidx.glance.background
 import androidx.glance.layout.Alignment
 import androidx.glance.layout.Column
 import androidx.glance.layout.Row
 import androidx.glance.layout.Spacer
 import androidx.glance.layout.fillMaxSize
 import androidx.glance.layout.fillMaxWidth
 import androidx.glance.layout.height
 import androidx.glance.layout.padding
 import androidx.glance.layout.width
 import androidx.glance.text.FontWeight
 import androidx.glance.text.Text
 import androidx.glance.text.TextStyle
 import androidx.glance.unit.ColorProvider
 import com.diajarkoding.duittracker.MainActivity
 import com.diajarkoding.duittracker.data.local.DuitTrackerDatabase
 import com.diajarkoding.duittracker.data.model.TransactionType
 import com.diajarkoding.duittracker.utils.CurrencyFormatter
 import kotlinx.coroutines.flow.first
 import kotlinx.datetime.Clock
 import kotlinx.datetime.LocalDate
 import kotlinx.datetime.TimeZone
 import kotlinx.datetime.toLocalDateTime
 
 class BalanceWidget : GlanceAppWidget() {
 
     override suspend fun provideGlance(context: Context, id: GlanceId) {
         val widgetData = getWidgetData(context)
         
         provideContent {
             BalanceWidgetContent(widgetData)
         }
     }
 
     private suspend fun getWidgetData(context: Context): WidgetData {
         return try {
             val database = DuitTrackerDatabase.getDatabase(context)
             val transactionDao = database.transactionDao()
             
             val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
             val today = now.date
             val startOfMonth = LocalDate(today.year, today.monthNumber, 1)
             val monthPrefix = "${today.year}-${today.monthNumber.toString().padStart(2, '0')}"
             
             val transactions = transactionDao.getAllTransactions().first()
             
             val monthTransactions = transactions.filter { 
                 it.transactionDate.startsWith(monthPrefix) 
             }
             
             val totalIncome = monthTransactions
                 .filter { it.type == TransactionType.INCOME.name }
                 .sumOf { it.amount }
             
             val totalExpense = monthTransactions
                 .filter { it.type == TransactionType.EXPENSE.name }
                 .sumOf { it.amount }
             
             val balance = totalIncome - totalExpense
             
             WidgetData(
                 balance = balance,
                 income = totalIncome,
                 expense = totalExpense,
                 monthName = "${today.month.name.take(3)} ${today.year}"
             )
         } catch (e: Exception) {
             WidgetData(0.0, 0.0, 0.0, "---")
         }
     }
 }
 
 data class WidgetData(
     val balance: Double,
     val income: Double,
     val expense: Double,
     val monthName: String
 )
 
 @Composable
 private fun BalanceWidgetContent(data: WidgetData) {
     Column(
         modifier = GlanceModifier
             .fillMaxSize()
             .background(Color(0xFFF5F5F4))
             .cornerRadius(16.dp)
             .padding(16.dp)
             .clickable(actionStartActivity<MainActivity>()),
         verticalAlignment = Alignment.Top,
         horizontalAlignment = Alignment.Start
     ) {
         // Header
         Row(
             modifier = GlanceModifier.fillMaxWidth(),
             horizontalAlignment = Alignment.Start,
             verticalAlignment = Alignment.CenterVertically
         ) {
             Text(
                 text = "DuitTracker",
                 style = TextStyle(
                     fontWeight = FontWeight.Bold,
                     fontSize = 14.sp,
                     color = ColorProvider(Color(0xFF525252))
                 )
             )
             Spacer(modifier = GlanceModifier.defaultWeight())
             Text(
                 text = data.monthName,
                 style = TextStyle(
                     fontSize = 12.sp,
                     color = ColorProvider(Color(0xFF525252))
                 )
             )
         }
         
         Spacer(modifier = GlanceModifier.height(12.dp))
         
         // Balance
         Text(
             text = CurrencyFormatter.format(data.balance),
             style = TextStyle(
                 fontWeight = FontWeight.Bold,
                 fontSize = 24.sp,
                 color = ColorProvider(Color(0xFF000000))
             )
         )
         
         Text(
             text = "Balance",
             style = TextStyle(
                 fontSize = 12.sp,
                 color = ColorProvider(Color(0xFF525252))
             )
         )
         
         Spacer(modifier = GlanceModifier.height(16.dp))
         
         // Income and Expense Row
         Row(
             modifier = GlanceModifier.fillMaxWidth(),
             horizontalAlignment = Alignment.Start
         ) {
             // Income
             Column(modifier = GlanceModifier.defaultWeight()) {
                 Text(
                     text = CurrencyFormatter.formatCompact(data.income),
                     style = TextStyle(
                         fontWeight = FontWeight.Bold,
                         fontSize = 16.sp,
                         color = ColorProvider(Color(0xFF22C55E))
                     )
                 )
                 Text(
                     text = "Income",
                     style = TextStyle(
                         fontSize = 10.sp,
                         color = ColorProvider(Color(0xFF525252))
                     )
                 )
             }
             
             Spacer(modifier = GlanceModifier.width(8.dp))
             
             // Expense
             Column(modifier = GlanceModifier.defaultWeight()) {
                 Text(
                     text = CurrencyFormatter.formatCompact(data.expense),
                     style = TextStyle(
                         fontWeight = FontWeight.Bold,
                         fontSize = 16.sp,
                         color = ColorProvider(Color(0xFFEF4444))
                     )
                 )
                 Text(
                     text = "Expense",
                     style = TextStyle(
                         fontSize = 10.sp,
                         color = ColorProvider(Color(0xFF525252))
                     )
                 )
             }
         }
     }
 }
