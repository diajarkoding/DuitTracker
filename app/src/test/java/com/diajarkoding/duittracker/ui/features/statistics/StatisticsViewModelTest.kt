 package com.diajarkoding.duittracker.ui.features.statistics
 
 import com.diajarkoding.duittracker.data.model.AccountSource
 import com.diajarkoding.duittracker.data.model.Transaction
 import com.diajarkoding.duittracker.data.model.TransactionCategory
 import com.diajarkoding.duittracker.data.model.TransactionType
 import com.diajarkoding.duittracker.data.repository.ExcelExportRepository
 import com.diajarkoding.duittracker.domain.model.TransactionResult
 import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
 import io.mockk.every
 import io.mockk.mockk
 import kotlinx.coroutines.Dispatchers
 import kotlinx.coroutines.ExperimentalCoroutinesApi
 import kotlinx.coroutines.flow.flowOf
 import kotlinx.coroutines.test.StandardTestDispatcher
 import kotlinx.coroutines.test.advanceUntilIdle
 import kotlinx.coroutines.test.resetMain
 import kotlinx.coroutines.test.runTest
 import kotlinx.coroutines.test.setMain
 import kotlinx.datetime.LocalDateTime
 import org.junit.After
 import org.junit.Assert.assertEquals
 import org.junit.Assert.assertFalse
 import org.junit.Assert.assertTrue
 import org.junit.Before
 import org.junit.Test
 
 @OptIn(ExperimentalCoroutinesApi::class)
 class StatisticsViewModelTest {
 
     private lateinit var viewModel: StatisticsViewModel
     private lateinit var transactionRepository: ITransactionRepository
     private lateinit var excelExportRepository: ExcelExportRepository
 
     private val testDispatcher = StandardTestDispatcher()
 
     private fun createTransaction(
         id: String,
         amount: Double,
         type: TransactionType,
         category: TransactionCategory,
         date: LocalDateTime
     ) = Transaction(
         id = id,
         userId = "user-1",
         amount = amount,
         category = category,
         type = type,
         accountSource = AccountSource.CASH,
         note = "Test",
         transactionDate = date
     )
 
     @Before
     fun setup() {
         Dispatchers.setMain(testDispatcher)
         transactionRepository = mockk(relaxed = true)
         excelExportRepository = mockk(relaxed = true)
 
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(emptyList()))
     }
 
     @After
     fun tearDown() {
         Dispatchers.resetMain()
     }
 
     @Test
     fun `initial state should have loading true`() = runTest {
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
 
         assertTrue(viewModel.uiState.value.isLoading)
     }
 
     @Test
     fun `loadTransactions should calculate expense by category`() = runTest {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, TransactionCategory.FOOD, LocalDateTime(2026, 1, 15, 10, 0)),
             createTransaction("2", 2000.0, TransactionType.EXPENSE, TransactionCategory.FOOD, LocalDateTime(2026, 1, 16, 12, 0)),
             createTransaction("3", 500.0, TransactionType.EXPENSE, TransactionCategory.TRANSPORT, LocalDateTime(2026, 1, 17, 14, 0))
         )
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(transactions))
 
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
         advanceUntilIdle()
 
         assertFalse(viewModel.uiState.value.isLoading)
         assertEquals(2, viewModel.uiState.value.expenseByCategory.size)
         assertEquals(TransactionCategory.FOOD, viewModel.uiState.value.expenseByCategory[0].category)
         assertEquals(3000.0, viewModel.uiState.value.expenseByCategory[0].amount, 0.01)
     }
 
     @Test
     fun `loadTransactions should calculate income by category`() = runTest {
         val transactions = listOf(
             createTransaction("1", 5000.0, TransactionType.INCOME, TransactionCategory.SALARY, LocalDateTime(2026, 1, 15, 10, 0)),
             createTransaction("2", 1000.0, TransactionType.INCOME, TransactionCategory.GIFT, LocalDateTime(2026, 1, 16, 12, 0))
         )
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(transactions))
 
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
         advanceUntilIdle()
 
         assertEquals(2, viewModel.uiState.value.incomeByCategory.size)
         assertEquals(6000.0, viewModel.uiState.value.totalIncome, 0.01)
     }
 
     @Test
     fun `loadTransactions error should update error state`() = runTest {
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Error("Network error"))
 
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
         advanceUntilIdle()
 
         assertFalse(viewModel.uiState.value.isLoading)
         assertEquals("Network error", viewModel.uiState.value.error)
     }
 
     @Test
     fun `setPeriod to ALL should show all time statistics`() = runTest {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, TransactionCategory.FOOD, LocalDateTime(2026, 1, 15, 10, 0)),
             createTransaction("2", 2000.0, TransactionType.EXPENSE, TransactionCategory.FOOD, LocalDateTime(2025, 12, 15, 10, 0))
         )
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(transactions))
 
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
         advanceUntilIdle()
 
         viewModel.setPeriod(StatisticsPeriod.ALL)
 
         assertEquals(StatisticsPeriod.ALL, viewModel.uiState.value.selectedPeriod)
         assertEquals("All Time", viewModel.uiState.value.selectedMonthName)
     }
 
     @Test
     fun `setPeriod to MONTHLY should filter by month`() = runTest {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, TransactionCategory.FOOD, LocalDateTime(2026, 1, 15, 10, 0))
         )
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(transactions))
 
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
         advanceUntilIdle()
 
         viewModel.setPeriod(StatisticsPeriod.MONTHLY)
 
         assertEquals(StatisticsPeriod.MONTHLY, viewModel.uiState.value.selectedPeriod)
     }
 
     @Test
     fun `setPeriod to WEEKLY should filter by week`() = runTest {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, TransactionCategory.FOOD, LocalDateTime(2026, 1, 15, 10, 0))
         )
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(transactions))
 
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
         advanceUntilIdle()
 
         viewModel.setPeriod(StatisticsPeriod.WEEKLY)
 
         assertEquals(StatisticsPeriod.WEEKLY, viewModel.uiState.value.selectedPeriod)
     }
 
     @Test
     fun `selectMonth should update selectedMonthIndex`() = runTest {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, TransactionCategory.FOOD, LocalDateTime(2026, 1, 15, 10, 0)),
             createTransaction("2", 2000.0, TransactionType.EXPENSE, TransactionCategory.FOOD, LocalDateTime(2025, 12, 15, 10, 0))
         )
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(transactions))
 
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
         advanceUntilIdle()
 
         viewModel.selectMonth(1)
 
         assertEquals(1, viewModel.uiState.value.selectedMonthIndex)
     }
 
     @Test
     fun `showExportDialog should set showExportDialog to true`() = runTest {
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
         advanceUntilIdle()
 
         viewModel.showExportDialog()
 
         assertTrue(viewModel.uiState.value.showExportDialog)
     }
 
     @Test
     fun `hideExportDialog should set showExportDialog to false`() = runTest {
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
         advanceUntilIdle()
 
         viewModel.showExportDialog()
         viewModel.hideExportDialog()
 
         assertFalse(viewModel.uiState.value.showExportDialog)
     }
 
     @Test
     fun `category percentage should be calculated correctly`() = runTest {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, TransactionCategory.FOOD, LocalDateTime(2026, 1, 15, 10, 0)),
             createTransaction("2", 1000.0, TransactionType.EXPENSE, TransactionCategory.TRANSPORT, LocalDateTime(2026, 1, 16, 12, 0))
         )
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(transactions))
 
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
         advanceUntilIdle()
 
         assertEquals(50.0f, viewModel.uiState.value.expenseByCategory[0].percentage, 0.1f)
         assertEquals(50.0f, viewModel.uiState.value.expenseByCategory[1].percentage, 0.1f)
     }
 
     @Test
     fun `empty transactions should result in empty categories`() = runTest {
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(emptyList()))
 
         viewModel = StatisticsViewModel(transactionRepository, excelExportRepository)
         advanceUntilIdle()
 
         assertTrue(viewModel.uiState.value.expenseByCategory.isEmpty())
         assertTrue(viewModel.uiState.value.incomeByCategory.isEmpty())
         assertEquals(0.0, viewModel.uiState.value.totalExpense, 0.01)
         assertEquals(0.0, viewModel.uiState.value.totalIncome, 0.01)
     }
 }
