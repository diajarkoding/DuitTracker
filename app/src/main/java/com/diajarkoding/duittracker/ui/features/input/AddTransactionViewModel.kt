package com.diajarkoding.duittracker.ui.features.input

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diajarkoding.duittracker.data.model.AccountSource
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionCategory
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.data.repository.ImageRepository
import com.diajarkoding.duittracker.domain.model.TransactionResult
import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "",
    val note: String = "",
    val description: String = "",
    val imageUri: Uri? = null,
    val isExpense: Boolean = true,
    val category: TransactionCategory = TransactionCategory.FOOD,
    val accountSource: AccountSource = AccountSource.CASH,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false
)

sealed class AddTransactionEvent {
    data class Success(val message: String) : AddTransactionEvent()
    data class Error(val message: String) : AddTransactionEvent()
    data object NavigateBack : AddTransactionEvent()
}

private const val TAG = "AddTransactionVM"

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: ITransactionRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddTransactionEvent>()
    val events: SharedFlow<AddTransactionEvent> = _events.asSharedFlow()

    init {
        observeNetworkState()
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            transactionRepository.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }
            }
        }
    }

    fun onAmountChange(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(amount = filtered) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun clearImage() {
        _uiState.update { it.copy(imageUri = null) }
    }

    fun onTypeChange(isExpense: Boolean) {
        val defaultCategory = if (isExpense) TransactionCategory.FOOD else TransactionCategory.SALARY
        _uiState.update { 
            it.copy(
                isExpense = isExpense,
                category = defaultCategory
            ) 
        }
    }

    fun onCategoryChange(category: TransactionCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun onAccountSourceChange(accountSource: AccountSource) {
        _uiState.update { it.copy(accountSource = accountSource) }
    }

    fun saveTransaction() {
        viewModelScope.launch {
            Log.d(TAG, "saveTransaction: Starting save process")
            val state = _uiState.value
            val amount = state.amount.toDoubleOrNull()

            Log.d(TAG, "saveTransaction: Amount=$amount, Note='${state.note}', Category=${state.category}")
            Log.d(TAG, "saveTransaction: IsExpense=${state.isExpense}, AccountSource=${state.accountSource}")
            Log.d(TAG, "saveTransaction: Description='${state.description}', ImageUri=${state.imageUri}")
            Log.d(TAG, "saveTransaction: IsOffline=${state.isOffline}")

            if (amount == null || amount <= 0) {
                Log.e(TAG, "saveTransaction: Invalid amount: $amount")
                _events.emit(AddTransactionEvent.Error("Please enter a valid amount"))
                return@launch
            }

            if (state.note.isBlank()) {
                Log.e(TAG, "saveTransaction: Note is blank")
                _events.emit(AddTransactionEvent.Error("Please enter a note"))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            val transactionId = UUID.randomUUID().toString()
            Log.d(TAG, "saveTransaction: Generated transaction ID: $transactionId")
            var imagePath: String? = null

            // Upload image if selected
            state.imageUri?.let { uri ->
                Log.d(TAG, "saveTransaction: Processing image upload for URI: $uri")
                if (state.isOffline) {
                    Log.d(TAG, "saveTransaction: Device is offline, saving image locally")
                    imageRepository.saveImageLocally(uri, transactionId)
                        .onSuccess {
                            Log.d(TAG, "saveTransaction: Local image save successful: $it")
                            imagePath = it
                        }
                        .onFailure {
                            Log.e(TAG, "saveTransaction: Local image save failed", it)
                        }
                } else {
                    Log.d(TAG, "saveTransaction: Device is online, uploading to Supabase")
                    imageRepository.uploadImage(uri)
                        .onSuccess {
                            Log.d(TAG, "saveTransaction: Image upload successful: $it")
                            imagePath = it
                        }
                        .onFailure {
                            Log.e(TAG, "saveTransaction: Image upload failed", it)
                            _events.emit(AddTransactionEvent.Error("Failed to upload image: ${it.message}"))
                        }
                }
            }

            Log.d(TAG, "saveTransaction: Final imagePath: $imagePath")

            val transaction = Transaction(
                id = transactionId,
                userId = "",
                amount = amount,
                category = state.category,
                type = if (state.isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
                accountSource = state.accountSource,
                note = state.note,
                description = state.description.ifBlank { null },
                imagePath = imagePath,
                transactionDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )

            Log.d(TAG, "saveTransaction: Created transaction object: $transaction")
            Log.d(TAG, "saveTransaction: Calling repository.addTransaction()")

            when (val result = transactionRepository.addTransaction(transaction)) {
                is TransactionResult.Success -> {
                    Log.d(TAG, "saveTransaction: Transaction added successfully: ${result.message}")
                    _events.emit(AddTransactionEvent.Success(
                        result.message ?: "Transaction added successfully"
                    ))
                    _events.emit(AddTransactionEvent.NavigateBack)
                }
                is TransactionResult.Error -> {
                    Log.e(TAG, "saveTransaction: Transaction add failed: ${result.message}")
                    _events.emit(AddTransactionEvent.Error(result.message))
                }
                else -> {
                    Log.w(TAG, "saveTransaction: Unexpected result type: $result")
                }
            }

            _uiState.update { it.copy(isLoading = false) }
            Log.d(TAG, "saveTransaction: Save process completed")
        }
    }
}
