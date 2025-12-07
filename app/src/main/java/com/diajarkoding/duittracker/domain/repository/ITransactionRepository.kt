package com.diajarkoding.duittracker.domain.repository

import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.domain.model.TransactionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

interface ITransactionRepository {
    val isOnline: StateFlow<Boolean>
    val pendingCount: Flow<Int>
    
    fun getAllTransactions(): Flow<TransactionResult<List<Transaction>>>
    fun getTransactionsByDate(date: LocalDate): Flow<TransactionResult<List<Transaction>>>
    fun getTransactionsByType(type: TransactionType): Flow<TransactionResult<List<Transaction>>>
    
    suspend fun addTransaction(transaction: Transaction): TransactionResult<Transaction>
    suspend fun updateTransaction(transaction: Transaction): TransactionResult<Transaction>
    suspend fun deleteTransaction(id: String): TransactionResult<Unit>
    suspend fun getTransactionById(id: String): TransactionResult<Transaction?>
    
    suspend fun refreshFromRemote(): TransactionResult<Unit>
    suspend fun syncPendingOperations(): TransactionResult<Int>
}
