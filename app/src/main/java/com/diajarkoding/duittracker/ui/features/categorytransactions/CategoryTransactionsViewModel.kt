package com.diajarkoding.duittracker.ui.features.categorytransactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionCategory
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.domain.model.TransactionResult
import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
import com.diajarkoding.duittracker.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import javax.inject.Inject

data class CategoryTransactionsUiState(
    val category: TransactionCategory = TransactionCategory.OTHER,
    val categoryName: String = "",
    val monthName: String = "",
    val isExpense: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CategoryTransactionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: ITransactionRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Routes.CategoryTransactions>()
    private val categoryName = route.category
    private val year = route.year
    private val month = route.month
    private val isExpense = route.isExpense

    private val _uiState = MutableStateFlow(CategoryTransactionsUiState())
    val uiState: StateFlow<CategoryTransactionsUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val category = try {
                TransactionCategory.valueOf(categoryName)
            } catch (e: Exception) {
                TransactionCategory.OTHER
            }

            val monthEnum = Month(month)
            val monthDisplayName = "${monthEnum.name.lowercase().replaceFirstChar { it.uppercase() }} $year"
            val categoryDisplayName = categoryName
                .split("_")
                .joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { it.uppercase() }
                }

            transactionRepository.getAllTransactions().collect { result ->
                when (result) {
                    is TransactionResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is TransactionResult.Success -> {
                        val startOfMonth = LocalDate(year, monthEnum, 1)
                        val endOfMonth = if (monthEnum == Month.DECEMBER) {
                            LocalDate(year + 1, Month.JANUARY, 1)
                        } else {
                            LocalDate(year, month + 1, 1)
                        }

                        val type = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME

                        val filteredTransactions = result.data
                            .filter { tx ->
                                val date = tx.transactionDate.date
                                tx.category == category &&
                                tx.type == type &&
                                date >= startOfMonth &&
                                date < endOfMonth
                            }
                            .sortedByDescending { it.transactionDate }

                        val totalAmount = filteredTransactions.sumOf { it.amount }

                        _uiState.update {
                            it.copy(
                                category = category,
                                categoryName = categoryDisplayName,
                                monthName = monthDisplayName,
                                isExpense = isExpense,
                                transactions = filteredTransactions,
                                totalAmount = totalAmount,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is TransactionResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }
}
