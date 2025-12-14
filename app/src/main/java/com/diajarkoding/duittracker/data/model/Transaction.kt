package com.diajarkoding.duittracker.data.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val amount: Double,
    val category: TransactionCategory,
    val type: TransactionType,
    @SerialName("account_source")
    val accountSource: AccountSource,
    val note: String,
    val description: String? = null,
    @SerialName("image_path")
    val imagePath: String? = null,
    @SerialName("transaction_date")
    val transactionDate: LocalDateTime,
    @SerialName("is_synced")
    val isSynced: Boolean = false,
    @SerialName("created_at")
    val createdAt: LocalDateTime? = null,
    @SerialName("updated_at")
    val updatedAt: LocalDateTime? = null
)

@Serializable
enum class TransactionType {
    @SerialName("expense")
    EXPENSE,
    @SerialName("income")
    INCOME
}

@Serializable
enum class TransactionCategory {
    @SerialName("food")
    FOOD,
    @SerialName("transport")
    TRANSPORT,
    @SerialName("shopping")
    SHOPPING,
    @SerialName("entertainment")
    ENTERTAINMENT,
    @SerialName("bills")
    BILLS,
    @SerialName("health")
    HEALTH,
    @SerialName("education")
    EDUCATION,
    @SerialName("social")
    SOCIAL,
    @SerialName("salary")
    SALARY,
    @SerialName("investment")
    INVESTMENT,
    @SerialName("daily_needs")
    DAILY_NEEDS,
    @SerialName("gift")
    GIFT,
    @SerialName("other")
    OTHER
}

@Serializable
enum class AccountSource {
    @SerialName("cash")
    CASH,
    @SerialName("bank")
    BANK,
    @SerialName("ewallet")
    EWALLET
}
