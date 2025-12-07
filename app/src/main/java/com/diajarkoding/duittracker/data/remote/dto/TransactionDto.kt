package com.diajarkoding.duittracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Transaction.
 * Maps exactly to Supabase 'transactions' table schema.
 * 
 * Supabase Table Schema:
 * - id: UUID PRIMARY KEY
 * - user_id: UUID NOT NULL (references auth.users)
 * - amount: DECIMAL(15,2) NOT NULL
 * - category: transaction_category ENUM NOT NULL
 * - type: transaction_type ENUM ('expense', 'income')
 * - account_source: account_source ENUM ('cash', 'bank', 'ewallet')
 * - note: TEXT NOT NULL
 * - description: TEXT
 * - image_path: TEXT
 * - transaction_date: TIMESTAMPTZ NOT NULL
 * - is_synced: BOOLEAN DEFAULT true
 * - created_at: TIMESTAMPTZ DEFAULT NOW()
 * - updated_at: TIMESTAMPTZ DEFAULT NOW()
 */
@Serializable
data class TransactionDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("amount")
    val amount: Double,

    @SerialName("category")
    val category: String,

    @SerialName("type")
    val type: String,

    @SerialName("account_source")
    val accountSource: String,

    @SerialName("note")
    val note: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("image_path")
    val imagePath: String? = null,

    @SerialName("transaction_date")
    val transactionDate: String,

    @SerialName("is_synced")
    val isSynced: Boolean = true,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * DTO for inserting new transaction (without server-generated fields).
 * Used for POST/INSERT operations to Supabase.
 */
@Serializable
data class TransactionInsertDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("amount")
    val amount: Double,

    @SerialName("category")
    val category: String,

    @SerialName("type")
    val type: String,

    @SerialName("account_source")
    val accountSource: String,

    @SerialName("note")
    val note: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("image_path")
    val imagePath: String? = null,

    @SerialName("transaction_date")
    val transactionDate: String,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * DTO for User Profile.
 * Maps to Supabase 'profiles' table.
 */
@Serializable
data class ProfileDto(
    @SerialName("id")
    val id: String,

    @SerialName("email")
    val email: String,

    @SerialName("name")
    val name: String,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("currency")
    val currency: String = "IDR",

    @SerialName("language")
    val language: String = "id",

    @SerialName("notifications_enabled")
    val notificationsEnabled: Boolean = true,

    @SerialName("monthly_budget")
    val monthlyBudget: Double? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)
