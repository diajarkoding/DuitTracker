package com.diajarkoding.duittracker.domain.model

sealed class TransactionResult<out T> {
    data class Success<T>(
        val data: T,
        val message: String? = null,
        val isFromCache: Boolean = false
    ) : TransactionResult<T>()
    
    data class Error(
        val message: String,
        val isOffline: Boolean = false,
        val exception: Throwable? = null
    ) : TransactionResult<Nothing>()
    
    data object Loading : TransactionResult<Nothing>()
}

fun <T, R> TransactionResult<T>.map(transform: (T) -> R): TransactionResult<R> {
    return when (this) {
        is TransactionResult.Success -> TransactionResult.Success(
            data = transform(data),
            message = message,
            isFromCache = isFromCache
        )
        is TransactionResult.Error -> this
        is TransactionResult.Loading -> this
    }
}

fun <T> TransactionResult<T>.onSuccess(action: (T) -> Unit): TransactionResult<T> {
    if (this is TransactionResult.Success) {
        action(data)
    }
    return this
}

fun <T> TransactionResult<T>.onError(action: (String) -> Unit): TransactionResult<T> {
    if (this is TransactionResult.Error) {
        action(message)
    }
    return this
}

fun <T> TransactionResult<T>.getOrNull(): T? {
    return when (this) {
        is TransactionResult.Success -> data
        else -> null
    }
}

fun <T> TransactionResult<T>.getOrDefault(default: T): T {
    return when (this) {
        is TransactionResult.Success -> data
        else -> default
    }
}
