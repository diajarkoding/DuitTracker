package com.diajarkoding.duittracker.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "pending_sync_work"
        const val AUTO_SYNC_WORK_NAME = "auto_sync_on_connect"
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync worker...")

        return when (val result = syncManager.syncPendingOperations()) {
            is SyncResult.Success -> {
                Log.d(TAG, "Sync successful: synced=${result.synced}, failed=${result.failed}")
                Result.success()
            }
            is SyncResult.Error -> {
                Log.e(TAG, "Sync error: ${result.message}")
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
            is SyncResult.Offline -> {
                Log.d(TAG, "Device offline, will retry later")
                Result.retry()
            }
            is SyncResult.NotAuthenticated -> {
                Log.d(TAG, "User not authenticated, skipping sync")
                Result.success()
            }
            is SyncResult.NoPendingOperations -> {
                Log.d(TAG, "No pending operations")
                Result.success()
            }
        }
    }
}
