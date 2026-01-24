 package com.diajarkoding.duittracker.domain.usecase
 
 import com.diajarkoding.duittracker.data.model.Transaction
 import com.diajarkoding.duittracker.data.model.TransactionType
 import kotlinx.datetime.LocalDate
 import kotlinx.datetime.Month
 import javax.inject.Inject
 
 data class MonthData(
     val monthKey: String,
     val year: Int,
     val month: Month,
     val transactions: List<Transaction>,
     val totalExpense: Double,
     val totalIncome: Double,
     val isExpanded: Boolean = false
 )
 
 class GroupTransactionsByDateUseCase @Inject constructor() {
     
     fun groupByDate(
         transactions: List<Transaction>,
         startOfMonth: LocalDate
     ): Map<LocalDate, List<Transaction>> {
         return transactions
             .filter { it.transactionDate.date >= startOfMonth }
             .groupBy { it.transactionDate.date }
             .toSortedMap(compareByDescending { it })
     }
 
     fun groupByMonth(
         transactions: List<Transaction>,
         expandedMonths: Set<String>
     ): List<MonthData> {
         return transactions
             .groupBy { tx ->
                 val date = tx.transactionDate.date
                 Triple(date.year, date.month, "${date.month.name.take(3)} ${date.year}")
             }
             .map { (key, txList) ->
                 val (year, month, monthKey) = key
                 MonthData(
                     monthKey = monthKey,
                     year = year,
                     month = month,
                     transactions = txList.sortedByDescending { it.transactionDate },
                     totalExpense = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                     totalIncome = txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                     isExpanded = expandedMonths.contains(monthKey)
                 )
             }
             .sortedByDescending { it.year * 100 + it.month.ordinal }
     }
 }
