 package com.diajarkoding.duittracker.domain.usecase
 
 import com.diajarkoding.duittracker.data.model.Transaction
 import com.diajarkoding.duittracker.data.model.TransactionType
 import kotlinx.datetime.LocalDate
 import javax.inject.Inject
 
 data class MonthlyStatistics(
     val totalIncome: Double,
     val totalExpense: Double,
     val balance: Double,
     val transactionCount: Int
 )
 
 class CalculateMonthlyStatisticsUseCase @Inject constructor() {
     operator fun invoke(
         transactions: List<Transaction>,
         startOfMonth: LocalDate
     ): MonthlyStatistics {
         val currentMonthTransactions = transactions.filter {
             it.transactionDate.date >= startOfMonth
         }
 
         val totalExpense = currentMonthTransactions
             .filter { it.type == TransactionType.EXPENSE }
             .sumOf { it.amount }
 
         val totalIncome = currentMonthTransactions
             .filter { it.type == TransactionType.INCOME }
             .sumOf { it.amount }
 
         return MonthlyStatistics(
             totalIncome = totalIncome,
             totalExpense = totalExpense,
             balance = totalIncome - totalExpense,
             transactionCount = currentMonthTransactions.size
         )
     }
 }
