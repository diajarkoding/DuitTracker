package com.diajarkoding.duittracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diajarkoding.duittracker.data.local.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {

    @Query("SELECT * FROM pending_operations ORDER BY created_at ASC")
    suspend fun getAllPending(): List<PendingOperationEntity>

    @Query("SELECT * FROM pending_operations ORDER BY created_at ASC")
    fun getAllPendingFlow(): Flow<List<PendingOperationEntity>>

    @Query("SELECT COUNT(*) FROM pending_operations")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM pending_operations WHERE entity_id = :entityId")
    suspend fun getPendingByEntityId(entityId: String): PendingOperationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: PendingOperationEntity)

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pending_operations WHERE entity_id = :entityId")
    suspend fun deleteByEntityId(entityId: String)

    @Query("UPDATE pending_operations SET retry_count = retry_count + 1 WHERE id = :id")
    suspend fun incrementRetry(id: String)

    @Query("DELETE FROM pending_operations WHERE retry_count >= :maxRetries")
    suspend fun deleteFailedOperations(maxRetries: Int = 3)

    @Query("DELETE FROM pending_operations")
    suspend fun deleteAll()
}
