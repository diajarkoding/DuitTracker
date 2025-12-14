package com.diajarkoding.duittracker.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    data object Splash : Routes

    @Serializable
    data object Login : Routes

    @Serializable
    data object Register : Routes

    @Serializable
    data object Dashboard : Routes

    @Serializable
    data object AddTransaction : Routes

    @Serializable
    data class TransactionDetail(val transactionId: String) : Routes

    @Serializable
    data class EditTransaction(val transactionId: String) : Routes

    @Serializable
    data object Statistics : Routes

    @Serializable
    data class CategoryTransactions(
        val category: String,
        val year: Int,
        val month: Int,
        val isExpense: Boolean
    ) : Routes
}
