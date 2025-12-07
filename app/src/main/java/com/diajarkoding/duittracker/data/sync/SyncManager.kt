package com.diajarkoding.duittracker.data.sync

import android.util.Log
import com.diajarkoding.duittracker.data.local.dao.PendingOperationDao
import com.diajarkoding.duittracker.data.local.dao.TransactionDao
import com.diajarkoding.duittracker.data.local.entity.OperationType
import com.diajarkoding.duittracker.data.mapper.TransactionMapper.toInsertDto
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.network.NetworkMonitor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

sealed class SyncResult {
    data class Success(val synced: Int, val failed: Int = 0) : SyncResult()
    data class Error(val message: String) : SyncResult()
    data object Offline : SyncResult()
    data object NotAuthenticated : SyncResult()
    data object NoPendingOperations : SyncResult()
}

@Singleton
class SyncManager @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val transactionDao: TransactionDao,
    private val supabaseClient: SupabaseClient,
    private val networkMonitor: NetworkMonitor
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val TABLE_TRANSACTIONS = "transactions"
    }

    suspend fun syncPendingOperations(): SyncResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting sync...")

        // Check network
        if (!networkMonitor.isOnline.value) {
            Log.d(TAG, "Device is offline, skipping sync")
            return@withContext SyncResult.Offline
        }

        // Check authentication
        val currentUser = supabaseClient.auth.currentUserOrNull()
        if (currentUser == null) {
            Log.d(TAG, "User not authenticated, skipping sync")
            return@withContext SyncResult.NotAuthenticated
        }

        // Get pending operations
        val pendingOps = pendingOperationDao.getAllPending()
        if (pendingOps.isEmpty()) {
            Log.d(TAG, "No pending operations to sync")
            return@withContext SyncResult.NoPendingOperations
        }

        Log.d(TAG, "Found ${pendingOps.size} pending operations")

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
                        Log.d(TAG, "Synced INSERT for ${op.entityId}")
                    }
                    OperationType.UPDATE.name -> {
                        val transaction = Json.decodeFromString<Transaction>(op.payload)
                        val dto = transaction.toInsertDto()
                        supabaseClient.postgrest[TABLE_TRANSACTIONS]
                            .update(dto) {
                                filter { eq("id", op.entityId) }
                            }
                        transactionDao.markAsSynced(op.entityId)
                        Log.d(TAG, "Synced UPDATE for ${op.entityId}")
                    }
                    OperationType.DELETE.name -> {
                        supabaseClient.postgrest[TABLE_TRANSACTIONS]
                            .delete {
                                filter { eq("id", op.entityId) }
                            }
                        Log.d(TAG, "Synced DELETE for ${op.entityId}")
                    }
                }
                pendingOperationDao.deleteById(op.id)
                syncedCount++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync operation ${op.id}: ${e.message}")
                pendingOperationDao.incrementRetry(op.id)
                failedCount++
            }
        }

        // Clean up failed operations (> 3 retries)
        pendingOperationDao.deleteFailedOperations()

        Log.d(TAG, "Sync complete: synced=$syncedCount, failed=$failedCount")
        SyncResult.Success(synced = syncedCount, failed = failedCount)
    }
}
