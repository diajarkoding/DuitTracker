package com.diajarkoding.duittracker.data.repository

import com.diajarkoding.duittracker.data.local.dao.PendingOperationDao
import com.diajarkoding.duittracker.data.local.dao.TransactionDao
import com.diajarkoding.duittracker.data.local.entity.OperationType
import com.diajarkoding.duittracker.data.local.entity.PendingOperationEntity
import com.diajarkoding.duittracker.data.mapper.TransactionMapper.toDomain
import com.diajarkoding.duittracker.data.mapper.TransactionMapper.toEntity
import com.diajarkoding.duittracker.data.mapper.TransactionMapper.toInsertDto
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.data.network.NetworkMonitor
import com.diajarkoding.duittracker.data.remote.dto.TransactionDto
import com.diajarkoding.duittracker.domain.model.TransactionResult
import com.diajarkoding.duittracker.domain.repository.ITransactionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val pendingOperationDao: PendingOperationDao,
    private val supabaseClient: SupabaseClient,
    private val networkMonitor: NetworkMonitor,
    private val imageRepository: ImageRepository
) : ITransactionRepository {

    companion object {
        private const val TABLE_TRANSACTIONS = "transactions"
    }

    override val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    override val pendingCount: Flow<Int> = pendingOperationDao.getPendingCount()

    private fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    // ==================== READ OPERATIONS ====================

    override fun getAllTransactions(): Flow<TransactionResult<List<Transaction>>> = flow {
        emit(TransactionResult.Loading)

        val userId = getCurrentUserId()
        if (userId == null) {
            emit(TransactionResult.Error("User not authenticated"))
            return@flow
        }

        if (networkMonitor.isOnline.value) {
            try {
                // ONLINE: Fetch from Supabase
                val remoteTransactions = supabaseClient.postgrest[TABLE_TRANSACTIONS]
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<TransactionDto>()

                // Cache to Room
                val entities = remoteTransactions.map { it.toEntity(isSynced = true) }
                transactionDao.deleteAllTransactions()
                transactionDao.insertTransactions(entities)

                val transactions = entities.map { it.toDomain() }
                    .sortedByDescending { it.transactionDate }
                emit(TransactionResult.Success(transactions))
            } catch (e: Exception) {
                // Fallback to cache on error
                val cachedTransactions = transactionDao.getTransactionsByUserId(userId)
                    .first()
                    .map { it.toDomain() }
                    .sortedByDescending { it.transactionDate }
                
                emit(TransactionResult.Success(
                    data = cachedTransactions,
                    message = "Couldn't refresh. Showing cached data.",
                    isFromCache = true
                ))
            }
        } else {
            // OFFLINE: Load from cache
            val cachedTransactions = transactionDao.getTransactionsByUserId(userId)
                .first()
                .map { it.toDomain() }
                .sortedByDescending { it.transactionDate }
            
            emit(TransactionResult.Success(
                data = cachedTransactions,
                message = "You're offline. Showing cached data.",
                isFromCache = true
            ))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTransactionsByDate(date: LocalDate): Flow<TransactionResult<List<Transaction>>> = flow {
        emit(TransactionResult.Loading)
        
        val datePrefix = date.toString()
        val transactions = transactionDao.getTransactionsByDate(datePrefix)
            .first()
            .map { it.toDomain() }
        
        emit(TransactionResult.Success(transactions))
    }.flowOn(Dispatchers.IO)

    override fun getTransactionsByType(type: TransactionType): Flow<TransactionResult<List<Transaction>>> = flow {
        emit(TransactionResult.Loading)
        
        val transactions = transactionDao.getTransactionsByType(type.name)
            .first()
            .map { it.toDomain() }
        
        emit(TransactionResult.Success(transactions))
    }.flowOn(Dispatchers.IO)

    override suspend fun getTransactionById(id: String): TransactionResult<Transaction?> = withContext(Dispatchers.IO) {
        try {
            val entity = transactionDao.getTransactionById(id)
            TransactionResult.Success(entity?.toDomain())
        } catch (e: Exception) {
            TransactionResult.Error("Failed to get transaction: ${e.message}")
        }
    }

    // ==================== WRITE OPERATIONS ====================

    override suspend fun addTransaction(transaction: Transaction): TransactionResult<Transaction> = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId()
        if (userId == null) {
            return@withContext TransactionResult.Error("User not authenticated")
        }

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val transactionWithUser = transaction.copy(
            userId = userId,
            createdAt = now,
            updatedAt = now
        )

        if (networkMonitor.isOnline.value) {
            // ONLINE: Save to Supabase directly
            try {
                val dto = transactionWithUser.toInsertDto()
                supabaseClient.postgrest[TABLE_TRANSACTIONS].insert(dto)

                // Cache to Room
                val entity = transactionWithUser.toEntity(isSynced = true)
                transactionDao.insertTransaction(entity)

                TransactionResult.Success(
                    data = transactionWithUser,
                    message = "Transaction added successfully"
                )
            } catch (e: Exception) {
                TransactionResult.Error("Failed to save transaction: ${e.message}")
            }
        } else {
            // OFFLINE: Save to Room + Add to pending queue
            try {
                val entity = transactionWithUser.toEntity(isSynced = false)
                transactionDao.insertTransaction(entity)
                
                addToPendingQueue(OperationType.INSERT, transactionWithUser)

                TransactionResult.Success(
                    data = transactionWithUser,
                    message = "Saved offline. Will sync when online."
                )
            } catch (e: Exception) {
                TransactionResult.Error("Failed to save offline: ${e.message}")
            }
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): TransactionResult<Transaction> = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val updatedTransaction = transaction.copy(updatedAt = now)

        if (networkMonitor.isOnline.value) {
            // ONLINE: Update on Supabase directly
            try {
                val dto = updatedTransaction.toInsertDto()
                supabaseClient.postgrest[TABLE_TRANSACTIONS]
                    .update(dto) {
                        filter { eq("id", transaction.id) }
                    }

                // Update cache
                val entity = updatedTransaction.toEntity(isSynced = true)
                transactionDao.updateTransaction(entity)

                TransactionResult.Success(
                    data = updatedTransaction,
                    message = "Transaction updated successfully"
                )
            } catch (e: Exception) {
                TransactionResult.Error("Failed to update transaction: ${e.message}")
            }
        } else {
            // OFFLINE: Update Room + Add to pending queue
            try {
                val entity = updatedTransaction.toEntity(isSynced = false)
                transactionDao.updateTransaction(entity)
                
                // Remove any existing pending operation for this entity
                pendingOperationDao.deleteByEntityId(transaction.id)
                addToPendingQueue(OperationType.UPDATE, updatedTransaction)

                TransactionResult.Success(
                    data = updatedTransaction,
                    message = "Updated offline. Will sync when online."
                )
            } catch (e: Exception) {
                TransactionResult.Error("Failed to update offline: ${e.message}")
            }
        }
    }

    override suspend fun deleteTransaction(id: String): TransactionResult<Unit> = withContext(Dispatchers.IO) {
        // Get the transaction first to check for image
        val existingTransaction = transactionDao.getTransactionById(id)
        val imagePath = existingTransaction?.imagePath
        
        if (networkMonitor.isOnline.value) {
            // ONLINE: Delete from Supabase directly
            try {
                // Delete image from Supabase storage if exists
                if (!imagePath.isNullOrBlank() && !imagePath.startsWith("/")) {
                    imageRepository.deleteImage(imagePath)
                }
                
                supabaseClient.postgrest[TABLE_TRANSACTIONS]
                    .delete {
                        filter { eq("id", id) }
                    }

                // Remove from cache
                transactionDao.deleteTransactionById(id)
                
                // Delete local image cache
                imageRepository.deleteLocalImage(id)

                TransactionResult.Success(
                    data = Unit,
                    message = "Transaction deleted successfully"
                )
            } catch (e: Exception) {
                TransactionResult.Error("Failed to delete transaction: ${e.message}")
            }
        } else {
            // OFFLINE: Mark for deletion + Add to pending queue
            try {
                if (existingTransaction != null) {
                    // Remove from local cache
                    transactionDao.deleteTransactionById(id)
                    
                    // Delete local image cache
                    imageRepository.deleteLocalImage(id)
                    
                    // Only add to pending if it was synced (exists on server)
                    if (existingTransaction.isSynced) {
                        // Store image path in payload for later deletion when syncing
                        val payload = if (!imagePath.isNullOrBlank() && !imagePath.startsWith("/")) {
                            "$id|$imagePath"
                        } else {
                            id
                        }
                        val pendingOp = PendingOperationEntity(
                            operationType = OperationType.DELETE.name,
                            entityId = id,
                            payload = payload,
                            createdAt = Clock.System.now().toString()
                        )
                        pendingOperationDao.insert(pendingOp)
                    } else {
                        // Remove any pending INSERT/UPDATE for this entity
                        pendingOperationDao.deleteByEntityId(id)
                    }
                }

                TransactionResult.Success(
                    data = Unit,
                    message = "Deleted offline. Will sync when online."
                )
            } catch (e: Exception) {
                TransactionResult.Error("Failed to delete offline: ${e.message}")
            }
        }
    }

    // ==================== SYNC OPERATIONS ====================

    override suspend fun refreshFromRemote(): TransactionResult<Unit> = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId()
        if (userId == null) {
            return@withContext TransactionResult.Error("User not authenticated")
        }

        if (!networkMonitor.isOnline.value) {
            return@withContext TransactionResult.Error("No internet connection", isOffline = true)
        }

        try {
            val remoteTransactions = supabaseClient.postgrest[TABLE_TRANSACTIONS]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<TransactionDto>()

            val entities = remoteTransactions.map { it.toEntity(isSynced = true) }
            transactionDao.deleteAllTransactions()
            transactionDao.insertTransactions(entities)

            TransactionResult.Success(Unit, "Data refreshed successfully")
        } catch (e: Exception) {
            TransactionResult.Error("Failed to refresh: ${e.message}")
        }
    }

    override suspend fun syncPendingOperations(): TransactionResult<Int> = withContext(Dispatchers.IO) {
        if (!networkMonitor.isOnline.value) {
            return@withContext TransactionResult.Error("No internet connection", isOffline = true)
        }

        val pendingOps = pendingOperationDao.getAllPending()
        if (pendingOps.isEmpty()) {
            return@withContext TransactionResult.Success(0, "No pending operations")
        }

        var syncedCount = 0
        var failedCount = 0

        for (op in pendingOps) {
            try {
                when (op.operationType) {
                    OperationType.INSERT.name -> {
                        val transaction = Json.decodeFromString<Transaction>(op.payload)
                        val dto = transaction.toInsertDto()
                        supabaseClient.postgrest[TABLE_TRANSACTIONS].insert(dto)
                        transactionDao.markAsSynced(op.entityId)
                    }
                    OperationType.UPDATE.name -> {
                        val transaction = Json.decodeFromString<Transaction>(op.payload)
                        val dto = transaction.toInsertDto()
                        supabaseClient.postgrest[TABLE_TRANSACTIONS]
                            .update(dto) {
                                filter { eq("id", op.entityId) }
                            }
                        transactionDao.markAsSynced(op.entityId)
                    }
                    OperationType.DELETE.name -> {
                        // Check if payload contains image path (format: "id|imagePath")
                        if (op.payload.contains("|")) {
                            val imagePath = op.payload.substringAfter("|")
                            if (imagePath.isNotBlank()) {
                                imageRepository.deleteImage(imagePath)
                            }
                        }
                        supabaseClient.postgrest[TABLE_TRANSACTIONS]
                            .delete {
                                filter { eq("id", op.entityId) }
                            }
                    }
                }
                pendingOperationDao.deleteById(op.id)
                syncedCount++
            } catch (e: Exception) {
                pendingOperationDao.incrementRetry(op.id)
                failedCount++
            }
        }

        // Clean up failed operations (> 3 retries)
        pendingOperationDao.deleteFailedOperations()

        val message = when {
            failedCount == 0 -> "Synced $syncedCount changes"
            syncedCount == 0 -> "Failed to sync $failedCount changes"
            else -> "Synced $syncedCount, failed $failedCount"
        }

        TransactionResult.Success(syncedCount, message)
    }

    // ==================== HELPER METHODS ====================

    private suspend fun addToPendingQueue(operationType: OperationType, transaction: Transaction) {
        val pendingOp = PendingOperationEntity(
            operationType = operationType.name,
            entityId = transaction.id,
            payload = Json.encodeToString(transaction),
            createdAt = Clock.System.now().toString()
        )
        pendingOperationDao.insert(pendingOp)
    }
}
