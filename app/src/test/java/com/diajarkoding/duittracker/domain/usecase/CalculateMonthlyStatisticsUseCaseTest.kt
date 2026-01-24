 package com.diajarkoding.duittracker.domain.usecase
 
 import com.diajarkoding.duittracker.data.model.AccountSource
 import com.diajarkoding.duittracker.data.model.Transaction
 import com.diajarkoding.duittracker.data.model.TransactionCategory
 import com.diajarkoding.duittracker.data.model.TransactionType
 import kotlinx.datetime.LocalDate
 import kotlinx.datetime.LocalDateTime
 import org.junit.Assert.assertEquals
 import org.junit.Before
 import org.junit.Test
 
 class CalculateMonthlyStatisticsUseCaseTest {
 
     private lateinit var useCase: CalculateMonthlyStatisticsUseCase
 
     @Before
     fun setup() {
         useCase = CalculateMonthlyStatisticsUseCase()
     }
 
     private fun createTransaction(
         id: String,
         amount: Double,
         type: TransactionType,
         date: LocalDateTime
     ) = Transaction(
         id = id,
         userId = "user-1",
         amount = amount,
         category = TransactionCategory.OTHER,
         type = type,
         accountSource = AccountSource.CASH,
         note = "Test",
         transactionDate = date
     )
 
     @Test
     fun `invoke with empty transactions should return zero statistics`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val result = useCase(emptyList(), startOfMonth)
 
         assertEquals(0.0, result.totalIncome, 0.01)
         assertEquals(0.0, result.totalExpense, 0.01)
         assertEquals(0.0, result.balance, 0.01)
         assertEquals(0, result.transactionCount)
     }
 
     @Test
     fun `invoke should calculate correct totals for current month`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val transactions = listOf(
             createTransaction("1", 5000.0, TransactionType.INCOME, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 6, 12, 0)),
             createTransaction("3", 2000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 7, 14, 0))
         )
 
         val result = useCase(transactions, startOfMonth)
 
         assertEquals(5000.0, result.totalIncome, 0.01)
         assertEquals(3000.0, result.totalExpense, 0.01)
         assertEquals(2000.0, result.balance, 0.01)
         assertEquals(3, result.transactionCount)
     }
 
     @Test
     fun `invoke should exclude transactions before start of month`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val transactions = listOf(
             createTransaction("1", 5000.0, TransactionType.INCOME, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 1000.0, TransactionType.EXPENSE, LocalDateTime(2025, 12, 31, 23, 59)),
             createTransaction("3", 2000.0, TransactionType.INCOME, LocalDateTime(2025, 12, 15, 14, 0))
         )
 
         val result = useCase(transactions, startOfMonth)
 
         assertEquals(5000.0, result.totalIncome, 0.01)
         assertEquals(0.0, result.totalExpense, 0.01)
         assertEquals(5000.0, result.balance, 0.01)
         assertEquals(1, result.transactionCount)
     }
 
     @Test
     fun `invoke should include transactions on start of month date`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.INCOME, LocalDateTime(2026, 1, 1, 0, 0))
         )
 
         val result = useCase(transactions, startOfMonth)
 
         assertEquals(1000.0, result.totalIncome, 0.01)
         assertEquals(1, result.transactionCount)
     }
 
     @Test
     fun `invoke should handle only expenses`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 2000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 6, 12, 0))
         )
 
         val result = useCase(transactions, startOfMonth)
 
         assertEquals(0.0, result.totalIncome, 0.01)
         assertEquals(3000.0, result.totalExpense, 0.01)
         assertEquals(-3000.0, result.balance, 0.01)
         assertEquals(2, result.transactionCount)
     }
 
     @Test
     fun `invoke should handle only income`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val transactions = listOf(
             createTransaction("1", 5000.0, TransactionType.INCOME, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 3000.0, TransactionType.INCOME, LocalDateTime(2026, 1, 6, 12, 0))
         )
 
         val result = useCase(transactions, startOfMonth)
 
         assertEquals(8000.0, result.totalIncome, 0.01)
         assertEquals(0.0, result.totalExpense, 0.01)
         assertEquals(8000.0, result.balance, 0.01)
         assertEquals(2, result.transactionCount)
     }
 
     @Test
     fun `invoke should handle decimal amounts`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val transactions = listOf(
             createTransaction("1", 1000.50, TransactionType.INCOME, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 500.25, TransactionType.EXPENSE, LocalDateTime(2026, 1, 6, 12, 0))
         )
 
         val result = useCase(transactions, startOfMonth)
 
         assertEquals(1000.50, result.totalIncome, 0.01)
         assertEquals(500.25, result.totalExpense, 0.01)
         assertEquals(500.25, result.balance, 0.01)
     }
 
     @Test
     fun `invoke should handle large amounts`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val transactions = listOf(
             createTransaction("1", 999_999_999.99, TransactionType.INCOME, LocalDateTime(2026, 1, 5, 10, 0))
         )
 
         val result = useCase(transactions, startOfMonth)
 
         assertEquals(999_999_999.99, result.totalIncome, 0.01)
     }
 }
