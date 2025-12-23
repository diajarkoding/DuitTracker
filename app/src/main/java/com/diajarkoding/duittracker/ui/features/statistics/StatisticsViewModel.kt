package com.diajarkoding.duittracker.ui.features.statistics

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionCategory
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.data.repository.ExcelExportRepository
import com.diajarkoding.duittracker.domain.model.TransactionResult
import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
import com.diajarkoding.duittracker.ui.components.SnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

enum class StatisticsPeriod {
    ALL, MONTHLY, WEEKLY
}

enum class ExportType {
    ALL_DATA, MONTHLY, WEEKLY, BY_CATEGORY
}

data class CategoryData(
    val category: TransactionCategory,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int,
    val transactions: List<Transaction> = emptyList()
)

data class MonthOption(
    val year: Int,
    val month: Month,
    val displayName: String
)

data class WeekOption(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val displayName: String
)

data class StatisticsUiState(
    val expenseByCategory: List<CategoryData> = emptyList(),
    val incomeByCategory: List<CategoryData> = emptyList(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val selectedPeriod: StatisticsPeriod = StatisticsPeriod.MONTHLY,
    val selectedMonthName: String = "",
    val availableMonths: List<MonthOption> = emptyList(),
    val selectedMonthIndex: Int = 0,
    val availableWeeks: List<WeekOption> = emptyList(),
    val selectedWeekIndex: Int = 0,
    val selectedWeekName: String = "",
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val error: String? = null,
    val showExportDialog: Boolean = false,
    val transactionCount: Int = 0,
    val allTimeIncome: Double = 0.0,
    val allTimeExpense: Double = 0.0
)

sealed class StatisticsEvent {
    data class ShowSnackbar(val message: String, val type: SnackbarType) : StatisticsEvent()
    data class ShareExcel(val intent: Intent) : StatisticsEvent()
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepository: ITransactionRepository,
    private val excelExportRepository: ExcelExportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StatisticsEvent>()
    val events: SharedFlow<StatisticsEvent> = _events.asSharedFlow()

    private var allTransactions: List<Transaction> = emptyList()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { result ->
                when (result) {
                    is TransactionResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is TransactionResult.Success -> {
                        allTransactions = result.data
                        processTransactions()
                        _uiState.update { it.copy(isLoading = false, error = null) }
                        
                        result.message?.let { message ->
                            val type = if (result.isFromCache) SnackbarType.WARNING else SnackbarType.INFO
                            _events.emit(StatisticsEvent.ShowSnackbar(message, type))
                        }
                    }
                    is TransactionResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                        _events.emit(StatisticsEvent.ShowSnackbar(result.message, SnackbarType.ERROR))
                    }
                }
            }
        }
    }

    private fun processTransactions() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        val selectedIndex = _uiState.value.selectedMonthIndex

        // Get available months from transactions
        val availableMonths = allTransactions
            .map { it.transactionDate.date }
            .distinctBy { "${it.year}-${it.monthNumber}" }
            .map { date ->
                MonthOption(
                    year = date.year,
                    month = date.month,
                    displayName = "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"
                )
            }
            .sortedByDescending { it.year * 100 + it.month.ordinal }
            .ifEmpty {
                listOf(
                    MonthOption(
                        year = today.year,
                        month = today.month,
                        displayName = "${today.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${today.year}"
                    )
                )
            }

        val safeIndex = selectedIndex.coerceIn(0, (availableMonths.size - 1).coerceAtLeast(0))
        val selectedMonth = availableMonths.getOrNull(safeIndex) ?: availableMonths.first()

        // Filter transactions for selected month
        val startOfMonth = LocalDate(selectedMonth.year, selectedMonth.month, 1)
        val endOfMonth = if (selectedMonth.month == Month.DECEMBER) {
            LocalDate(selectedMonth.year + 1, Month.JANUARY, 1)
        } else {
            LocalDate(selectedMonth.year, selectedMonth.month.ordinal + 2, 1)
        }

        val monthTransactions = allTransactions.filter { tx ->
            val date = tx.transactionDate.date
            date >= startOfMonth && date < endOfMonth
        }

        // Separate expense and income
        val expenses = monthTransactions.filter { it.type == TransactionType.EXPENSE }
        val incomes = monthTransactions.filter { it.type == TransactionType.INCOME }

        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = incomes.sumOf { it.amount }

        // Group expenses by category
        val expenseByCategory = expenses
            .groupBy { it.category }
            .map { (category, txList) ->
                val amount = txList.sumOf { it.amount }
                CategoryData(
                    category = category,
                    amount = amount,
                    percentage = if (totalExpense > 0) (amount / totalExpense * 100).toFloat() else 0f,
                    transactionCount = txList.size,
                    transactions = txList.sortedByDescending { it.transactionDate }
                )
            }
            .sortedByDescending { it.amount }

        // Group income by category
        val incomeByCategory = incomes
            .groupBy { it.category }
            .map { (category, txList) ->
                val amount = txList.sumOf { it.amount }
                CategoryData(
                    category = category,
                    amount = amount,
                    percentage = if (totalIncome > 0) (amount / totalIncome * 100).toFloat() else 0f,
                    transactionCount = txList.size,
                    transactions = txList.sortedByDescending { it.transactionDate }
                )
            }
            .sortedByDescending { it.amount }

        _uiState.update { state ->
            state.copy(
                expenseByCategory = expenseByCategory,
                incomeByCategory = incomeByCategory,
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                selectedMonthName = selectedMonth.displayName,
                availableMonths = availableMonths,
                selectedMonthIndex = safeIndex
            )
        }
    }

    fun selectMonth(index: Int) {
        _uiState.update { it.copy(selectedMonthIndex = index) }
        processTransactions()
    }

    fun previousMonth() {
        val currentIndex = _uiState.value.selectedMonthIndex
        val maxIndex = _uiState.value.availableMonths.size - 1
        if (currentIndex < maxIndex) {
            selectMonth(currentIndex + 1)
        }
    }

    fun nextMonth() {
        val currentIndex = _uiState.value.selectedMonthIndex
        if (currentIndex > 0) {
            selectMonth(currentIndex - 1)
        }
    }

    fun exportToExcel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            val state = _uiState.value
            val selectedMonth = state.availableMonths.getOrNull(state.selectedMonthIndex)
                ?: return@launch

            // Filter transactions for selected month
            val startOfMonth = LocalDate(selectedMonth.year, selectedMonth.month, 1)
            val endOfMonth = if (selectedMonth.month == Month.DECEMBER) {
                LocalDate(selectedMonth.year + 1, Month.JANUARY, 1)
            } else {
                LocalDate(selectedMonth.year, selectedMonth.month.ordinal + 2, 1)
            }

            val monthTransactions = allTransactions.filter { tx ->
                val date = tx.transactionDate.date
                date >= startOfMonth && date < endOfMonth
            }

            val result = excelExportRepository.exportToExcel(
                transactions = monthTransactions,
                expenseByCategory = state.expenseByCategory,
                incomeByCategory = state.incomeByCategory,
                totalExpense = state.totalExpense,
                totalIncome = state.totalIncome,
                monthName = state.selectedMonthName
            )

            result.onSuccess { uri ->
                val shareIntent = excelExportRepository.createShareIntent(uri)
                _events.emit(StatisticsEvent.ShareExcel(shareIntent))
                _events.emit(StatisticsEvent.ShowSnackbar("Report exported successfully!", SnackbarType.SUCCESS))
            }.onFailure { error ->
                _events.emit(StatisticsEvent.ShowSnackbar("Export failed: ${error.message}", SnackbarType.ERROR))
            }

            _uiState.update { it.copy(isExporting = false) }
        }
    }

    fun setPeriod(period: StatisticsPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        when (period) {
            StatisticsPeriod.ALL -> processAllTimeStatistics()
            StatisticsPeriod.MONTHLY -> processTransactions()
            StatisticsPeriod.WEEKLY -> processWeeklyStatistics()
        }
    }

    private fun processAllTimeStatistics() {
        val expenses = allTransactions.filter { it.type == TransactionType.EXPENSE }
        val incomes = allTransactions.filter { it.type == TransactionType.INCOME }

        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = incomes.sumOf { it.amount }

        val expenseByCategory = expenses
            .groupBy { it.category }
            .map { (category, txList) ->
                val amount = txList.sumOf { it.amount }
                CategoryData(
                    category = category,
                    amount = amount,
                    percentage = if (totalExpense > 0) (amount / totalExpense * 100).toFloat() else 0f,
                    transactionCount = txList.size,
                    transactions = txList.sortedByDescending { it.transactionDate }
                )
            }
            .sortedByDescending { it.amount }

        val incomeByCategory = incomes
            .groupBy { it.category }
            .map { (category, txList) ->
                val amount = txList.sumOf { it.amount }
                CategoryData(
                    category = category,
                    amount = amount,
                    percentage = if (totalIncome > 0) (amount / totalIncome * 100).toFloat() else 0f,
                    transactionCount = txList.size,
                    transactions = txList.sortedByDescending { it.transactionDate }
                )
            }
            .sortedByDescending { it.amount }

        _uiState.update { state ->
            state.copy(
                expenseByCategory = expenseByCategory,
                incomeByCategory = incomeByCategory,
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                selectedMonthName = "All Time",
                transactionCount = allTransactions.size,
                allTimeIncome = totalIncome,
                allTimeExpense = totalExpense
            )
        }
    }

    private fun processWeeklyStatistics() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        val selectedIndex = _uiState.value.selectedWeekIndex

        // Generate available weeks from transactions
        val availableWeeks = allTransactions
            .map { it.transactionDate.date }
            .map { getWeekRange(it) }
            .distinctBy { "${it.first}" }
            .map { (start, end) ->
                WeekOption(
                    startDate = start,
                    endDate = end,
                    displayName = "${formatShortDate(start)} - ${formatShortDate(end)}"
                )
            }
            .sortedByDescending { it.startDate }
            .ifEmpty {
                val (start, end) = getWeekRange(today)
                listOf(WeekOption(start, end, "${formatShortDate(start)} - ${formatShortDate(end)}"))
            }

        val safeIndex = selectedIndex.coerceIn(0, (availableWeeks.size - 1).coerceAtLeast(0))
        val selectedWeek = availableWeeks.getOrNull(safeIndex) ?: availableWeeks.first()

        val weekTransactions = allTransactions.filter { tx ->
            val date = tx.transactionDate.date
            date >= selectedWeek.startDate && date <= selectedWeek.endDate
        }

        val expenses = weekTransactions.filter { it.type == TransactionType.EXPENSE }
        val incomes = weekTransactions.filter { it.type == TransactionType.INCOME }

        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = incomes.sumOf { it.amount }

        val expenseByCategory = expenses
            .groupBy { it.category }
            .map { (category, txList) ->
                val amount = txList.sumOf { it.amount }
                CategoryData(
                    category = category,
                    amount = amount,
                    percentage = if (totalExpense > 0) (amount / totalExpense * 100).toFloat() else 0f,
                    transactionCount = txList.size,
                    transactions = txList.sortedByDescending { it.transactionDate }
                )
            }
            .sortedByDescending { it.amount }

        val incomeByCategory = incomes
            .groupBy { it.category }
            .map { (category, txList) ->
                val amount = txList.sumOf { it.amount }
                CategoryData(
                    category = category,
                    amount = amount,
                    percentage = if (totalIncome > 0) (amount / totalIncome * 100).toFloat() else 0f,
                    transactionCount = txList.size,
                    transactions = txList.sortedByDescending { it.transactionDate }
                )
            }
            .sortedByDescending { it.amount }

        _uiState.update { state ->
            state.copy(
                expenseByCategory = expenseByCategory,
                incomeByCategory = incomeByCategory,
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                availableWeeks = availableWeeks,
                selectedWeekIndex = safeIndex,
                selectedWeekName = selectedWeek.displayName,
                transactionCount = weekTransactions.size
            )
        }
    }

    private fun getWeekRange(date: LocalDate): Pair<LocalDate, LocalDate> {
        val dayOfWeek = date.dayOfWeek.isoDayNumber
        val startOfWeek = date.minus(dayOfWeek - 1, DateTimeUnit.DAY)
        val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)
        return Pair(startOfWeek, endOfWeek)
    }

    private fun formatShortDate(date: LocalDate): String {
        return "${date.dayOfMonth}/${date.monthNumber}"
    }

    fun selectWeek(index: Int) {
        _uiState.update { it.copy(selectedWeekIndex = index) }
        processWeeklyStatistics()
    }

    fun previousWeek() {
        val currentIndex = _uiState.value.selectedWeekIndex
        val maxIndex = _uiState.value.availableWeeks.size - 1
        if (currentIndex < maxIndex) {
            selectWeek(currentIndex + 1)
        }
    }

    fun nextWeek() {
        val currentIndex = _uiState.value.selectedWeekIndex
        if (currentIndex > 0) {
            selectWeek(currentIndex - 1)
        }
    }

    fun showExportDialog() {
        _uiState.update { it.copy(showExportDialog = true) }
    }

    fun hideExportDialog() {
        _uiState.update { it.copy(showExportDialog = false) }
    }

    fun exportWithType(exportType: ExportType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, showExportDialog = false) }

            val state = _uiState.value

            val result = when (exportType) {
                ExportType.ALL_DATA -> {
                    excelExportRepository.exportToExcel(
                        transactions = allTransactions,
                        expenseByCategory = state.expenseByCategory,
                        incomeByCategory = state.incomeByCategory,
                        totalExpense = allTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                        totalIncome = allTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                        monthName = "All_Data"
                    )
                }
                ExportType.MONTHLY -> {
                    val selectedMonth = state.availableMonths.getOrNull(state.selectedMonthIndex)
                    if (selectedMonth != null) {
                        val startOfMonth = LocalDate(selectedMonth.year, selectedMonth.month, 1)
                        val endOfMonth = if (selectedMonth.month == Month.DECEMBER) {
                            LocalDate(selectedMonth.year + 1, Month.JANUARY, 1)
                        } else {
                            LocalDate(selectedMonth.year, selectedMonth.month.ordinal + 2, 1)
                        }
                        val monthTransactions = allTransactions.filter { tx ->
                            val date = tx.transactionDate.date
                            date >= startOfMonth && date < endOfMonth
                        }
                        excelExportRepository.exportToExcel(
                            transactions = monthTransactions,
                            expenseByCategory = state.expenseByCategory,
                            incomeByCategory = state.incomeByCategory,
                            totalExpense = state.totalExpense,
                            totalIncome = state.totalIncome,
                            monthName = state.selectedMonthName
                        )
                    } else {
                        Result.failure(Exception("No month selected"))
                    }
                }
                ExportType.WEEKLY -> {
                    val selectedWeek = state.availableWeeks.getOrNull(state.selectedWeekIndex)
                    if (selectedWeek != null) {
                        val weekTransactions = allTransactions.filter { tx ->
                            val date = tx.transactionDate.date
                            date >= selectedWeek.startDate && date <= selectedWeek.endDate
                        }
                        excelExportRepository.exportToExcel(
                            transactions = weekTransactions,
                            expenseByCategory = state.expenseByCategory,
                            incomeByCategory = state.incomeByCategory,
                            totalExpense = state.totalExpense,
                            totalIncome = state.totalIncome,
                            monthName = "Week_${selectedWeek.displayName.replace("/", "-").replace(" ", "")}"
                        )
                    } else {
                        Result.failure(Exception("No week selected"))
                    }
                }
                ExportType.BY_CATEGORY -> {
                    excelExportRepository.exportToExcel(
                        transactions = allTransactions,
                        expenseByCategory = state.expenseByCategory,
                        incomeByCategory = state.incomeByCategory,
                        totalExpense = state.totalExpense,
                        totalIncome = state.totalIncome,
                        monthName = "By_Category"
                    )
                }
            }

            result.onSuccess { uri ->
                val shareIntent = excelExportRepository.createShareIntent(uri)
                _events.emit(StatisticsEvent.ShareExcel(shareIntent))
                _events.emit(StatisticsEvent.ShowSnackbar("Report exported successfully!", SnackbarType.SUCCESS))
            }.onFailure { error ->
                _events.emit(StatisticsEvent.ShowSnackbar("Export failed: ${error.message}", SnackbarType.ERROR))
            }

            _uiState.update { it.copy(isExporting = false) }
        }
    }
}
