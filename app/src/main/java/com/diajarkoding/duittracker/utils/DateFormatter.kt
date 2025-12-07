package com.diajarkoding.duittracker.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

object DateFormatter {
    private val today: LocalDate
        get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val yesterday: LocalDate
        get() = today.minus(1, DateTimeUnit.DAY)

    fun formatDateHeader(date: LocalDate): String {
        return when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                val dayOfWeek = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                "$dayOfWeek, ${date.dayOfMonth} $month ${date.year}"
            }
        }
    }

    fun formatTime(dateTime: LocalDateTime): String {
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    fun formatShortDate(date: LocalDate): String {
        val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        return "${date.dayOfMonth} $month"
    }

    fun formatFullDate(date: LocalDate): String {
        val dayOfWeek = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$dayOfWeek, ${date.dayOfMonth} $month ${date.year}"
    }
}
