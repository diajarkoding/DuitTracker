package com.diajarkoding.duittracker.data.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val avatarUrl: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

@Serializable
data class UserProfile(
    val id: String,
    val userId: String,
    val currency: String = "IDR",
    val language: String = "id",
    val notificationsEnabled: Boolean = true,
    val monthlyBudget: Double? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
