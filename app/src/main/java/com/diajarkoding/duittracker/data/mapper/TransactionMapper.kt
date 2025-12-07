package com.diajarkoding.duittracker.data.mapper

import com.diajarkoding.duittracker.data.local.entity.TransactionEntity
import com.diajarkoding.duittracker.data.model.AccountSource
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionCategory
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.data.remote.dto.TransactionDto
import com.diajarkoding.duittracker.data.remote.dto.TransactionInsertDto
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Mapper object for converting between Transaction data representations:
 * - TransactionEntity (Room/Local DB)
 * - TransactionDto (Supabase/Network)
 * - Transaction (Domain Model)
 */
object TransactionMapper {

    // ==================== Entity <-> DTO ====================

    /**
     * Convert Room Entity to Supabase DTO for upload.
     * Used when syncing local data to server.
     */
    fun TransactionEntity.toDto(): TransactionDto {
        return TransactionDto(
            id = id,
            userId = userId,
            amount = amount,
            category = category.lowercase(),
            type = type.lowercase(),
            accountSource = accountSource.lowercase(),
            note = note,
            description = description,
            imagePath = imagePath,
            transactionDate = transactionDate,
            isSynced = true,
            createdAt = createdAt,
            updatedAt = updatedAt ?: getCurrentTimestamp()
        )
    }

    /**
     * Convert Room Entity to Insert DTO (for upsert operations).
     */
    fun TransactionEntity.toInsertDto(): TransactionInsertDto {
        return TransactionInsertDto(
            id = id,
            userId = userId,
            amount = amount,
            category = category.lowercase(),
            type = type.lowercase(),
            accountSource = accountSource.lowercase(),
            note = note,
            description = description,
            imagePath = imagePath,
            transactionDate = transactionDate,
            updatedAt = updatedAt ?: getCurrentTimestamp()
        )
    }

    /**
     * Convert Supabase DTO to Room Entity for local storage.
     * Used when pulling data from server.
     * 
     * @param isSynced Whether this data is synced (default true for server data)
     */
    fun TransactionDto.toEntity(isSynced: Boolean = true): TransactionEntity {
        return TransactionEntity(
            id = id,
            userId = userId,
            amount = amount,
            category = category.uppercase(),
            type = type.uppercase(),
            accountSource = accountSource.uppercase(),
            note = note,
            description = description,
            imagePath = imagePath,
            transactionDate = transactionDate,
            isSynced = isSynced,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    // ==================== Entity <-> Domain ====================

    /**
     * Convert Room Entity to Domain Model.
     * Used when presenting data to UI.
     */
    fun TransactionEntity.toDomain(): Transaction {
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
            transactionDate = parseTimestamp(transactionDate),
            isSynced = isSynced,
            createdAt = createdAt?.let { parseTimestamp(it) },
            updatedAt = updatedAt?.let { parseTimestamp(it) }
        )
    }

    /**
     * Convert Domain Model to Room Entity.
     * Used when saving user input to local DB.
     * 
     * @param isSynced Whether this transaction has been synced to server
     */
    fun Transaction.toEntity(isSynced: Boolean = false): TransactionEntity {
        val now = getCurrentTimestamp()
        return TransactionEntity(
            id = id,
            userId = userId,
            amount = amount,
            category = category.name,
            type = type.name,
            accountSource = accountSource.name,
            note = note,
            description = description,
            imagePath = imagePath,
            transactionDate = transactionDate.toString(),
            isSynced = isSynced,
            createdAt = createdAt?.toString() ?: now,
            updatedAt = now
        )
    }

    // ==================== DTO <-> Domain ====================

    /**
     * Convert Supabase DTO to Domain Model.
     */
    fun TransactionDto.toDomain(): Transaction {
        return Transaction(
            id = id,
            userId = userId,
            amount = amount,
            category = TransactionCategory.valueOf(category.uppercase()),
            type = TransactionType.valueOf(type.uppercase()),
            accountSource = AccountSource.valueOf(accountSource.uppercase()),
            note = note,
            description = description,
            imagePath = imagePath,
            transactionDate = LocalDateTime.parse(transactionDate.substringBefore("+")),
            isSynced = isSynced,
            createdAt = createdAt?.let { parseTimestamp(it) },
            updatedAt = updatedAt?.let { parseTimestamp(it) }
        )
    }

    /**
     * Convert Domain Model to Supabase DTO.
     */
    fun Transaction.toDto(): TransactionDto {
        return TransactionDto(
            id = id,
            userId = userId,
            amount = amount,
            category = category.name.lowercase(),
            type = type.name.lowercase(),
            accountSource = accountSource.name.lowercase(),
            note = note,
            description = description,
            imagePath = imagePath,
            transactionDate = transactionDate.toString(),
            isSynced = isSynced,
            createdAt = createdAt?.toString(),
            updatedAt = updatedAt?.toString() ?: getCurrentTimestamp()
        )
    }

    /**
     * Convert Domain Model to Insert DTO (for upsert operations).
     */
    fun Transaction.toInsertDto(): TransactionInsertDto {
        return TransactionInsertDto(
            id = id,
            userId = userId,
            amount = amount,
            category = category.name.lowercase(),
            type = type.name.lowercase(),
            accountSource = accountSource.name.lowercase(),
            note = note,
            description = description,
            imagePath = imagePath,
            transactionDate = transactionDate.toString(),
            updatedAt = updatedAt?.toString() ?: getCurrentTimestamp()
        )
    }

    // ==================== Batch Conversions ====================

    fun List<TransactionEntity>.toDtoList(): List<TransactionDto> = map { it.toDto() }
    fun List<TransactionEntity>.toInsertDtoList(): List<TransactionInsertDto> = map { it.toInsertDto() }
    fun List<TransactionDto>.toEntityList(isSynced: Boolean = true): List<TransactionEntity> = map { it.toEntity(isSynced) }
    
    @JvmName("entityListToDomain")
    fun List<TransactionEntity>.toDomainList(): List<Transaction> = map { it.toDomain() }
    
    @JvmName("dtoListToDomain")
    fun List<TransactionDto>.toDomainList(): List<Transaction> = map { it.toDomain() }

    // ==================== Utility Functions ====================

    /**
     * Get current timestamp in ISO-8601 format.
     */
    private fun getCurrentTimestamp(): String {
        return Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
    }

    /**
     * Parse timestamp string, handling various formats from Supabase.
     */
    private fun parseTimestamp(timestamp: String): LocalDateTime {
        return try {
            // Handle Supabase timestamp format: "2024-01-01T12:00:00+00:00"
            LocalDateTime.parse(timestamp.substringBefore("+").substringBefore("Z"))
        } catch (e: Exception) {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }

    /**
     * Compare timestamps for conflict resolution.
     * Returns true if remote is newer than local.
     */
    fun isRemoteNewer(localUpdatedAt: String?, remoteUpdatedAt: String?): Boolean {
        if (remoteUpdatedAt == null) return false
        if (localUpdatedAt == null) return true

        return try {
            val local = parseTimestamp(localUpdatedAt)
            val remote = parseTimestamp(remoteUpdatedAt)
            val localInstant = local.toInstant(TimeZone.currentSystemDefault())
            val remoteInstant = remote.toInstant(TimeZone.currentSystemDefault())
            remoteInstant > localInstant
        } catch (e: Exception) {
            true // Default to accepting remote on parse error
        }
    }
}
