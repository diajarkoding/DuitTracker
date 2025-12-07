package com.diajarkoding.duittracker.ui.features.edit

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.diajarkoding.duittracker.data.model.AccountSource
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionCategory
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.data.repository.ImageRepository
import com.diajarkoding.duittracker.domain.model.TransactionResult
import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
import com.diajarkoding.duittracker.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditTransactionUiState(
    val transactionId: String = "",
    val amount: String = "",
    val note: String = "",
    val description: String = "",
    val imageUri: Uri? = null,
    val existingImagePath: String? = null,
    val existingImageUrl: String? = null,
    val isExpense: Boolean = true,
    val category: TransactionCategory = TransactionCategory.FOOD,
    val accountSource: AccountSource = AccountSource.CASH,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isOffline: Boolean = false,
    val originalTransaction: Transaction? = null
)

sealed class EditTransactionEvent {
    data class Success(val message: String) : EditTransactionEvent()
    data class Error(val message: String) : EditTransactionEvent()
    data object NavigateBack : EditTransactionEvent()
}

private const val TAG = "EditTransactionVM"

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: ITransactionRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val transactionId: String = savedStateHandle.toRoute<Routes.EditTransaction>().transactionId

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditTransactionEvent>()
    val events: SharedFlow<EditTransactionEvent> = _events.asSharedFlow()

    init {
        observeNetworkState()
        loadTransaction()
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            transactionRepository.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }
            }
        }
    }

    private fun loadTransaction() {
        viewModelScope.launch {
            Log.d(TAG, "loadTransaction: Loading transaction with ID: $transactionId")
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = transactionRepository.getTransactionById(transactionId)) {
                is TransactionResult.Success -> {
                    result.data?.let { transaction ->
                        Log.d(TAG, "loadTransaction: Transaction loaded successfully")
                        Log.d(TAG, "loadTransaction: Transaction data: $transaction")
                        Log.d(TAG, "loadTransaction: Image path: ${transaction.imagePath}")
                        
                        // Resolve image URL if there's an image path
                        var imageUrl: String? = null
                        if (!transaction.imagePath.isNullOrBlank()) {
                            if (transaction.imagePath.startsWith("/")) {
                                Log.d(TAG, "loadTransaction: Using local file path")
                                imageUrl = transaction.imagePath
                            } else {
                                Log.d(TAG, "loadTransaction: Resolving Supabase URL")
                                imageUrl = imageRepository.getImageUrl(transaction.imagePath)
                                Log.d(TAG, "loadTransaction: Resolved URL: $imageUrl")
                            }
                        }
                        
                        _uiState.update {
                            it.copy(
                                transactionId = transaction.id,
                                amount = transaction.amount.toLong().toString(),
                                note = transaction.note,
                                description = transaction.description ?: "",
                                existingImagePath = transaction.imagePath,
                                existingImageUrl = imageUrl,
                                isExpense = transaction.type == TransactionType.EXPENSE,
                                category = transaction.category,
                                accountSource = transaction.accountSource,
                                isLoading = false,
                                originalTransaction = transaction
                            )
                        }
                    } ?: run {
                        Log.e(TAG, "loadTransaction: Transaction not found")
                        _events.emit(EditTransactionEvent.Error("Transaction not found"))
                        _events.emit(EditTransactionEvent.NavigateBack)
                    }
                }
                is TransactionResult.Error -> {
                    Log.e(TAG, "loadTransaction: Error loading transaction: ${result.message}")
                    _events.emit(EditTransactionEvent.Error(result.message))
                    _events.emit(EditTransactionEvent.NavigateBack)
                }
                else -> {
                    Log.w(TAG, "loadTransaction: Unexpected result type: $result")
                }
            }
        }
    }

    fun onAmountChange(amount: String) {
        val filtered = amount.filter { it.isDigit() }
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
        _uiState.update { it.copy(imageUri = null, existingImagePath = null) }
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
            Log.d(TAG, "saveTransaction: Starting update process")
            val state = _uiState.value
            val originalTransaction = state.originalTransaction

            Log.d(TAG, "saveTransaction: TransactionId=${state.transactionId}")
            Log.d(TAG, "saveTransaction: Amount=${state.amount}, Note='${state.note}'")
            Log.d(TAG, "saveTransaction: Category=${state.category}, IsExpense=${state.isExpense}")
            Log.d(TAG, "saveTransaction: Description='${state.description}'")
            Log.d(TAG, "saveTransaction: ExistingImagePath=${state.existingImagePath}")
            Log.d(TAG, "saveTransaction: NewImageUri=${state.imageUri}")
            Log.d(TAG, "saveTransaction: IsOffline=${state.isOffline}")
            
            if (originalTransaction == null) {
                Log.e(TAG, "saveTransaction: Original transaction is null")
                _events.emit(EditTransactionEvent.Error("Transaction not found"))
                return@launch
            }

            val amount = state.amount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Log.e(TAG, "saveTransaction: Invalid amount: $amount")
                _events.emit(EditTransactionEvent.Error("Please enter a valid amount"))
                return@launch
            }

            if (state.note.isBlank()) {
                Log.e(TAG, "saveTransaction: Note is blank")
                _events.emit(EditTransactionEvent.Error("Please enter a note"))
                return@launch
            }

            _uiState.update { it.copy(isSaving = true) }

            // Handle image upload
            var imagePath: String? = state.existingImagePath
            Log.d(TAG, "saveTransaction: Initial imagePath from existing: $imagePath")
            
            state.imageUri?.let { uri ->
                Log.d(TAG, "saveTransaction: New image selected, processing upload for URI: $uri")
                if (state.isOffline) {
                    Log.d(TAG, "saveTransaction: Device is offline, saving image locally")
                    imageRepository.saveImageLocally(uri, state.transactionId)
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
                            _events.emit(EditTransactionEvent.Error("Failed to upload image: ${it.message}"))
                        }
                }
            }

            Log.d(TAG, "saveTransaction: Final imagePath: $imagePath")

            val updatedTransaction = originalTransaction.copy(
                amount = amount,
                category = state.category,
                type = if (state.isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
                accountSource = state.accountSource,
                note = state.note,
                description = state.description.ifBlank { null },
                imagePath = imagePath
            )

            Log.d(TAG, "saveTransaction: Updated transaction object: $updatedTransaction")
            Log.d(TAG, "saveTransaction: Calling repository.updateTransaction()")

            when (val result = transactionRepository.updateTransaction(updatedTransaction)) {
                is TransactionResult.Success -> {
                    Log.d(TAG, "saveTransaction: Transaction updated successfully: ${result.message}")
                    _events.emit(EditTransactionEvent.Success(
                        result.message ?: "Transaction updated successfully"
                    ))
                    _events.emit(EditTransactionEvent.NavigateBack)
                }
                is TransactionResult.Error -> {
                    Log.e(TAG, "saveTransaction: Transaction update failed: ${result.message}")
                    _events.emit(EditTransactionEvent.Error(result.message))
                }
                else -> {
                    Log.w(TAG, "saveTransaction: Unexpected result type: $result")
                }
            }

            _uiState.update { it.copy(isSaving = false) }
            Log.d(TAG, "saveTransaction: Update process completed")
        }
    }
}
