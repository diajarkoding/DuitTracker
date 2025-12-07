package com.diajarkoding.duittracker.ui.features.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.diajarkoding.duittracker.data.model.Transaction
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

private const val TAG = "TransactionDetailVM"

data class TransactionDetailUiState(
    val transaction: Transaction? = null,
    val imageUrl: String? = null,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val error: String? = null
)

sealed class TransactionDetailEvent {
    data class Deleted(val message: String) : TransactionDetailEvent()
    data class Error(val message: String) : TransactionDetailEvent()
}

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: ITransactionRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Routes.TransactionDetail>()
    private val transactionId = route.transactionId

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TransactionDetailEvent>()
    val events: SharedFlow<TransactionDetailEvent> = _events.asSharedFlow()

    init {
        loadTransaction()
    }

    private fun loadTransaction() {
        viewModelScope.launch {
            Log.d(TAG, "loadTransaction: Loading transaction ID: $transactionId")
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = transactionRepository.getTransactionById(transactionId)) {
                is TransactionResult.Success -> {
                    if (result.data != null) {
                        val transaction = result.data
                        Log.d(TAG, "loadTransaction: Transaction loaded: $transaction")
                        Log.d(TAG, "loadTransaction: Image path: ${transaction.imagePath}")
                        
                        // Resolve image URL if there's an image path
                        var imageUrl: String? = null
                        if (!transaction.imagePath.isNullOrBlank()) {
                            // Check if it's a local file path
                            if (transaction.imagePath.startsWith("/")) {
                                Log.d(TAG, "loadTransaction: Using local file path")
                                imageUrl = transaction.imagePath
                            } else {
                                // It's a Supabase storage path, get the public URL
                                Log.d(TAG, "loadTransaction: Resolving Supabase URL for path: ${transaction.imagePath}")
                                imageUrl = imageRepository.getImageUrl(transaction.imagePath)
                                Log.d(TAG, "loadTransaction: Resolved URL: $imageUrl")
                            }
                        }
                        
                        _uiState.update { 
                            it.copy(
                                transaction = transaction,
                                imageUrl = imageUrl,
                                isLoading = false,
                                error = null
                            )
                        }
                    } else {
                        Log.e(TAG, "loadTransaction: Transaction not found")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Transaction not found"
                            )
                        }
                    }
                }
                is TransactionResult.Error -> {
                    Log.e(TAG, "loadTransaction: Error: ${result.message}")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            
            when (val result = transactionRepository.deleteTransaction(transactionId)) {
                is TransactionResult.Success -> {
                    _events.emit(TransactionDetailEvent.Deleted(
                        result.message ?: "Transaction deleted successfully"
                    ))
                }
                is TransactionResult.Error -> {
                    _uiState.update { it.copy(isDeleting = false) }
                    _events.emit(TransactionDetailEvent.Error(result.message))
                }
                else -> {}
            }
        }
    }
}
