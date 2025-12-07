package com.diajarkoding.duittracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diajarkoding.duittracker.data.model.AccountSource
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionCategory
import com.diajarkoding.duittracker.data.model.TransactionType
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    val amount: Double,
    
    val category: String,
    
    val type: String,
    
    @ColumnInfo(name = "account_source")
    val accountSource: String,
    
    val note: String,
    
    val description: String? = null,
    
    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,
    
    @ColumnInfo(name = "transaction_date")
    val transactionDate: String,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null
) {
    fun toTransaction(): Transaction {
        return Transaction(
            id = id,
            userId = userId,
            amount = amount,
            category = TransactionCategory.valueOf(category),
            type = TransactionType.valueOf(type),
            accountSource = AccountSource.valueOf(accountSource),
            note = note,
            description = description,
            imagePath = imagePath,
            transactionDate = LocalDateTime.parse(transactionDate),
            isSynced = isSynced,
            createdAt = createdAt?.let { LocalDateTime.parse(it) },
            updatedAt = updatedAt?.let { LocalDateTime.parse(it) }
        )
    }

    companion object {
        fun fromTransaction(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                userId = transaction.userId,
                amount = transaction.amount,
                category = transaction.category.name,
                type = transaction.type.name,
                accountSource = transaction.accountSource.name,
                note = transaction.note,
                description = transaction.description,
                imagePath = transaction.imagePath,
                transactionDate = transaction.transactionDate.toString(),
                isSynced = transaction.isSynced,
                createdAt = transaction.createdAt?.toString(),
                updatedAt = transaction.updatedAt?.toString()
            )
        }
    }
}
