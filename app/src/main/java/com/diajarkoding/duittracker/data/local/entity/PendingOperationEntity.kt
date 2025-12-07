package com.diajarkoding.duittracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class OperationType {
    INSERT,
    UPDATE,
    DELETE
}

@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "operation_type")
    val operationType: String,
    
    @ColumnInfo(name = "entity_type")
    val entityType: String = "TRANSACTION",
    
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    
    @ColumnInfo(name = "payload")
    val payload: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0
)
