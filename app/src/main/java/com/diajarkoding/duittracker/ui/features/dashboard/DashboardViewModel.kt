package com.diajarkoding.duittracker.ui.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.domain.model.TransactionResult
import com.diajarkoding.duittracker.domain.repository.IAuthRepository
import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
import com.diajarkoding.duittracker.ui.components.SnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

enum class ViewMode {
    DAILY,
    MONTHLY
}

data class MonthData(
    val monthKey: String,
    val year: Int,
    val month: Month,
    val transactions: List<Transaction>,
    val totalExpense: Double,
    val totalIncome: Double,
    val isExpanded: Boolean = false
)

data class DashboardUiState(
    val userName: String = "",
    val greeting: String = "",
    val currentMonthTransactions: List<Transaction> = emptyList(),
    val groupedTransactions: Map<LocalDate, List<Transaction>> = emptyMap(),
    val monthlyData: List<MonthData> = emptyList(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val balance: Double = 0.0,
    val currentMonthName: String = "",
    val viewMode: ViewMode = ViewMode.DAILY,
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val pendingCount: Int = 0,
    val error: String? = null
)

sealed class DashboardEvent {
    data object LoggedOut : DashboardEvent()
    data class ShowSnackbar(val message: String, val type: SnackbarType) : DashboardEvent()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: ITransactionRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.DAILY)
    private val _expandedMonths = MutableStateFlow<Set<String>>(emptySet())
    
    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    init {
        observeNetworkState()
        observePendingCount()
        observeUser()
        loadTransactions()
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            var wasOffline = !transactionRepository.isOnline.value
            transactionRepository.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }
                if (isOnline && wasOffline && _uiState.value.pendingCount > 0) {
                    syncPendingOperations()
                }
                wasOffline = !isOnline
            }
        }
    }

    private fun observePendingCount() {
        viewModelScope.launch {
            transactionRepository.pendingCount.collect { count ->
                _uiState.update { it.copy(pendingCount = count) }
            }
        }
    }

    private fun observeUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val hour = now.hour
                val greeting = when (hour) {
                    in 5..11 -> "Good morning"
                    in 12..16 -> "Good afternoon"
                    in 17..20 -> "Good evening"
                    else -> "Good night"
                }
                val userName = user?.name?.split(" ")?.firstOrNull() ?: "User"
                _uiState.update { it.copy(userName = userName, greeting = greeting) }
            }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { result ->
                when (result) {
                    is TransactionResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is TransactionResult.Success -> {
                        processTransactions(result.data)
                        _uiState.update { it.copy(isLoading = false, error = null) }
                        
                        // Show message if from cache
                        result.message?.let { message ->
                            val type = if (result.isFromCache) SnackbarType.WARNING else SnackbarType.INFO
                            _events.emit(DashboardEvent.ShowSnackbar(message, type))
                        }
                    }
                    is TransactionResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                        _events.emit(DashboardEvent.ShowSnackbar(result.message, SnackbarType.ERROR))
                    }
                }
            }
        }
    }

    private fun processTransactions(transactions: List<Transaction>) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        val startOfMonth = LocalDate(today.year, today.monthNumber, 1)
        val currentMonthName = "${today.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${today.year}"

        val currentMonthTransactions = transactions.filter { 
            it.transactionDate.date >= startOfMonth 
        }

        val totalExpense = currentMonthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val totalIncome = currentMonthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val groupedByDate = currentMonthTransactions
            .groupBy { it.transactionDate.date }
            .toSortedMap(compareByDescending { it })

        val expandedMonths = _expandedMonths.value
        val monthlyData = transactions
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

        _uiState.update { state ->
            state.copy(
                currentMonthTransactions = currentMonthTransactions,
                groupedTransactions = groupedByDate,
                monthlyData = monthlyData,
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                balance = totalIncome - totalExpense,
                currentMonthName = currentMonthName,
                viewMode = _viewMode.value
            )
        }
    }

    private fun syncPendingOperations() {
        viewModelScope.launch {
            when (val result = transactionRepository.syncPendingOperations()) {
                is TransactionResult.Success -> {
                    if (result.data > 0) {
                        _events.emit(DashboardEvent.ShowSnackbar(
                            result.message ?: "Synced $result.data changes",
                            SnackbarType.SUCCESS
                        ))
                        loadTransactions()
                    }
                }
                is TransactionResult.Error -> {
                    _events.emit(DashboardEvent.ShowSnackbar(result.message, SnackbarType.ERROR))
                }
                else -> {}
            }
        }
    }

    fun refresh() {
        loadTransactions()
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.update { mode }
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun toggleMonthExpansion(monthKey: String) {
        _expandedMonths.update { current ->
            if (current.contains(monthKey)) {
                current - monthKey
            } else {
                current + monthKey
            }
        }
        // Update UI state with new expansion state
        _uiState.update { state ->
            state.copy(
                monthlyData = state.monthlyData.map { data ->
                    if (data.monthKey == monthKey) {
                        data.copy(isExpanded = !data.isExpanded)
                    } else {
                        data
                    }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _events.emit(DashboardEvent.LoggedOut)
        }
    }
}
