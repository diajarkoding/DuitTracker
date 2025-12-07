package com.diajarkoding.duittracker.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val indonesianFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    fun format(amount: Double): String {
        return indonesianFormat.format(amount)
            .replace("Rp", "Rp ")
            .replace(",00", "")
    }

    fun formatCompact(amount: Double): String {
        return when {
            amount >= 1_000_000_000 -> String.format("Rp %.1fB", amount / 1_000_000_000)
            amount >= 1_000_000 -> String.format("Rp %.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("Rp %.1fK", amount / 1_000)
            else -> format(amount)
        }
    }

    fun formatWithSign(amount: Double, isExpense: Boolean): String {
        val sign = if (isExpense) "-" else "+"
        return "$sign ${format(amount)}"
    }
}
