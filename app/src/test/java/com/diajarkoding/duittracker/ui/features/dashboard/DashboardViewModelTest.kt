 package com.diajarkoding.duittracker.ui.features.dashboard
 
 import app.cash.turbine.test
 import com.diajarkoding.duittracker.R
 import com.diajarkoding.duittracker.data.model.AccountSource
 import com.diajarkoding.duittracker.data.model.Transaction
 import com.diajarkoding.duittracker.data.model.TransactionCategory
 import com.diajarkoding.duittracker.data.model.TransactionType
 import com.diajarkoding.duittracker.domain.model.TransactionResult
 import com.diajarkoding.duittracker.domain.repository.IAuthRepository
 import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
 import io.mockk.coEvery
 import io.mockk.coVerify
 import io.mockk.every
 import io.mockk.mockk
 import kotlinx.coroutines.Dispatchers
 import kotlinx.coroutines.ExperimentalCoroutinesApi
 import kotlinx.coroutines.flow.MutableStateFlow
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
 class DashboardViewModelTest {
 
     private lateinit var viewModel: DashboardViewModel
     private lateinit var transactionRepository: ITransactionRepository
     private lateinit var authRepository: IAuthRepository
 
     private val testDispatcher = StandardTestDispatcher()
 
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
 
     @Before
     fun setup() {
         Dispatchers.setMain(testDispatcher)
         transactionRepository = mockk(relaxed = true)
         authRepository = mockk(relaxed = true)
 
         every { transactionRepository.isOnline } returns MutableStateFlow(true)
         every { transactionRepository.pendingCount } returns flowOf(0)
         every { authRepository.currentUser } returns flowOf(null)
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(emptyList()))
     }
 
     @After
     fun tearDown() {
         Dispatchers.resetMain()
     }
 
     @Test
     fun `initial state should have loading true`() = runTest {
         viewModel = DashboardViewModel(transactionRepository, authRepository)
         
         assertTrue(viewModel.uiState.value.isLoading)
     }
 
     @Test
     fun `loadTransactions should update uiState with transactions`() = runTest {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 15, 10, 0)),
             createTransaction("2", 5000.0, TransactionType.INCOME, LocalDateTime(2026, 1, 16, 12, 0))
         )
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(transactions))
 
         viewModel = DashboardViewModel(transactionRepository, authRepository)
         advanceUntilIdle()
 
         assertFalse(viewModel.uiState.value.isLoading)
         assertEquals(2, viewModel.uiState.value.currentMonthTransactions.size)
     }
 
     @Test
     fun `loadTransactions should calculate correct totals`() = runTest {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 15, 10, 0)),
             createTransaction("2", 5000.0, TransactionType.INCOME, LocalDateTime(2026, 1, 16, 12, 0)),
             createTransaction("3", 500.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 17, 14, 0))
         )
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(transactions))
 
         viewModel = DashboardViewModel(transactionRepository, authRepository)
         advanceUntilIdle()
 
         assertEquals(5000.0, viewModel.uiState.value.totalIncome, 0.01)
         assertEquals(1500.0, viewModel.uiState.value.totalExpense, 0.01)
         assertEquals(3500.0, viewModel.uiState.value.balance, 0.01)
     }
 
     @Test
     fun `loadTransactions error should update error state`() = runTest {
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Error("Network error"))
 
         viewModel = DashboardViewModel(transactionRepository, authRepository)
         advanceUntilIdle()
 
         assertFalse(viewModel.uiState.value.isLoading)
         assertEquals("Network error", viewModel.uiState.value.error)
     }
 
     @Test
     fun `setViewMode should update viewMode in state`() = runTest {
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(emptyList()))
 
         viewModel = DashboardViewModel(transactionRepository, authRepository)
         advanceUntilIdle()
 
         viewModel.setViewMode(ViewMode.MONTHLY)
 
         assertEquals(ViewMode.MONTHLY, viewModel.uiState.value.viewMode)
     }
 
     @Test
     fun `toggleMonthExpansion should toggle month expansion state`() = runTest {
         val transactions = listOf(
             createTransaction("1", 1000.0, TransactionType.EXPENSE, LocalDateTime(2026, 1, 15, 10, 0))
         )
         every { transactionRepository.getAllTransactions() } returns flowOf(TransactionResult.Success(transactions))
 
         viewModel = DashboardViewModel(transactionRepository, authRepository)
         advanceUntilIdle()
 
         val monthKey = viewModel.uiState.value.monthlyData.firstOrNull()?.monthKey
         if (monthKey != null) {
             assertFalse(viewModel.uiState.value.monthlyData.first().isExpanded)
 
             viewModel.toggleMonthExpansion(monthKey)
 
             assertTrue(viewModel.uiState.value.monthlyData.first().isExpanded)
 
             viewModel.toggleMonthExpansion(monthKey)
 
             assertFalse(viewModel.uiState.value.monthlyData.first().isExpanded)
         }
     }
 
     @Test
     fun `offline state should update isOffline in uiState`() = runTest {
         val isOnlineFlow = MutableStateFlow(false)
         every { transactionRepository.isOnline } returns isOnlineFlow
 
         viewModel = DashboardViewModel(transactionRepository, authRepository)
         advanceUntilIdle()
 
         assertTrue(viewModel.uiState.value.isOffline)
     }
 
     @Test
     fun `pendingCount should update in uiState`() = runTest {
         every { transactionRepository.pendingCount } returns flowOf(5)
 
         viewModel = DashboardViewModel(transactionRepository, authRepository)
         advanceUntilIdle()
 
         assertEquals(5, viewModel.uiState.value.pendingCount)
     }
 
     @Test
     fun `logout should emit LoggedOut event`() = runTest {
         viewModel = DashboardViewModel(transactionRepository, authRepository)
         advanceUntilIdle()
 
         viewModel.events.test {
             viewModel.logout()
             val event = awaitItem()
             assertTrue(event is DashboardEvent.LoggedOut)
             cancelAndIgnoreRemainingEvents()
         }
 
         coVerify { authRepository.logout() }
     }
 
     @Test
     fun `refresh should call loadTransactions`() = runTest {
         viewModel = DashboardViewModel(transactionRepository, authRepository)
         advanceUntilIdle()
 
         viewModel.refresh()
         advanceUntilIdle()
 
         coVerify(atLeast = 2) { transactionRepository.getAllTransactions() }
     }
 }
