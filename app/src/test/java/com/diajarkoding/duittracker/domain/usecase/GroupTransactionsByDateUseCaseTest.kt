 package com.diajarkoding.duittracker.domain.usecase
 
 import com.diajarkoding.duittracker.data.model.AccountSource
 import com.diajarkoding.duittracker.data.model.Transaction
 import com.diajarkoding.duittracker.data.model.TransactionCategory
 import com.diajarkoding.duittracker.data.model.TransactionType
 import kotlinx.datetime.LocalDate
 import kotlinx.datetime.LocalDateTime
 import kotlinx.datetime.Month
 import org.junit.Assert.assertEquals
 import org.junit.Assert.assertTrue
 import org.junit.Before
 import org.junit.Test
 
 class GroupTransactionsByDateUseCaseTest {
 
     private lateinit var useCase: GroupTransactionsByDateUseCase
 
     @Before
     fun setup() {
         useCase = GroupTransactionsByDateUseCase()
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
     fun `groupByDate with empty transactions should return empty map`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val result = useCase.groupByDate(emptyList(), startOfMonth)
 
         assertTrue(result.isEmpty())
     }
 
     @Test
     fun `groupByDate should group transactions by date`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 2000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 5, 14, 0)),
             createTransaction("3", 3000.0, TransactionType.INCOME, LocalDateTime(2026, 1, 6, 10, 0))
         )
 
         val result = useCase.groupByDate(transactions, startOfMonth)
 
         assertEquals(2, result.size)
         assertEquals(2, result[LocalDate(2026, 1, 5)]?.size)
         assertEquals(1, result[LocalDate(2026, 1, 6)]?.size)
     }
 
     @Test
     fun `groupByDate should exclude transactions before start of month`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 2000.0, TransactionType.EXPENSE, LocalDateTime(2025, 12, 31, 23, 59))
         )
 
         val result = useCase.groupByDate(transactions, startOfMonth)
 
         assertEquals(1, result.size)
         assertTrue(result.containsKey(LocalDate(2026, 1, 5)))
     }
 
     @Test
     fun `groupByDate should sort by date descending`() {
         val startOfMonth = LocalDate(2026, 1, 1)
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 2000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 10, 10, 0)),
             createTransaction("3", 3000.0, TransactionType.INCOME, LocalDateTime(2026, 1, 3, 10, 0))
         )
 
         val result = useCase.groupByDate(transactions, startOfMonth)
         val keys = result.keys.toList()
 
         assertEquals(LocalDate(2026, 1, 10), keys[0])
         assertEquals(LocalDate(2026, 1, 5), keys[1])
         assertEquals(LocalDate(2026, 1, 3), keys[2])
     }
 
     @Test
     fun `groupByMonth with empty transactions should return empty list`() {
         val result = useCase.groupByMonth(emptyList(), emptySet())
 
         assertTrue(result.isEmpty())
     }
 
     @Test
     fun `groupByMonth should group transactions by month`() {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 2000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 15, 10, 0)),
             createTransaction("3", 3000.0, TransactionType.INCOME, LocalDateTime(2026, 2, 5, 10, 0))
         )
 
         val result = useCase.groupByMonth(transactions, emptySet())
 
         assertEquals(2, result.size)
     }
 
     @Test
     fun `groupByMonth should calculate correct totals`() {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 2000.0, TransactionType.INCOME, LocalDateTime(2026, 1, 15, 10, 0))
         )
 
         val result = useCase.groupByMonth(transactions, emptySet())
 
         assertEquals(1, result.size)
         assertEquals(1000.0, result[0].totalExpense, 0.01)
         assertEquals(2000.0, result[0].totalIncome, 0.01)
     }
 
     @Test
     fun `groupByMonth should respect expanded months`() {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 5, 10, 0))
         )
 
         val expandedMonths = setOf("JAN 2026")
         val result = useCase.groupByMonth(transactions, expandedMonths)
 
         assertEquals(1, result.size)
         assertTrue(result[0].isExpanded)
     }
 
     @Test
     fun `groupByMonth should sort by year and month descending`() {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 2000.0, TransactionType.EXPENSE, LocalDateTime(2026, 3, 5, 10, 0)),
             createTransaction("3", 3000.0, TransactionType.INCOME, LocalDateTime(2025, 12, 5, 10, 0))
         )
 
         val result = useCase.groupByMonth(transactions, emptySet())
 
         assertEquals(3, result.size)
         assertEquals(Month.MARCH, result[0].month)
         assertEquals(2026, result[0].year)
         assertEquals(Month.JANUARY, result[1].month)
         assertEquals(Month.DECEMBER, result[2].month)
         assertEquals(2025, result[2].year)
     }
 
     @Test
     fun `groupByMonth should sort transactions within month by date descending`() {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 5, 10, 0)),
             createTransaction("2", 2000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 20, 10, 0)),
             createTransaction("3", 3000.0, TransactionType.INCOME, LocalDateTime(2026, 1, 10, 10, 0))
         )
 
         val result = useCase.groupByMonth(transactions, emptySet())
 
         assertEquals(1, result.size)
         assertEquals("2", result[0].transactions[0].id)
         assertEquals("3", result[0].transactions[1].id)
         assertEquals("1", result[0].transactions[2].id)
     }
 }
