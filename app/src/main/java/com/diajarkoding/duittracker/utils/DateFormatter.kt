package com.diajarkoding.duittracker.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.diajarkoding.duittracker.R
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

    @StringRes
    private fun getDayOfWeekResId(dayOfWeek: kotlinx.datetime.DayOfWeek): Int {
        return when (dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> R.string.day_monday
            kotlinx.datetime.DayOfWeek.TUESDAY -> R.string.day_tuesday
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> R.string.day_wednesday
            kotlinx.datetime.DayOfWeek.THURSDAY -> R.string.day_thursday
            kotlinx.datetime.DayOfWeek.FRIDAY -> R.string.day_friday
            kotlinx.datetime.DayOfWeek.SATURDAY -> R.string.day_saturday
            kotlinx.datetime.DayOfWeek.SUNDAY -> R.string.day_sunday
            else -> R.string.day_monday
        }
    }

    @StringRes
    private fun getMonthResId(month: kotlinx.datetime.Month): Int {
        return when (month) {
            kotlinx.datetime.Month.JANUARY -> R.string.month_january
            kotlinx.datetime.Month.FEBRUARY -> R.string.month_february
            kotlinx.datetime.Month.MARCH -> R.string.month_march
            kotlinx.datetime.Month.APRIL -> R.string.month_april
            kotlinx.datetime.Month.MAY -> R.string.month_may
            kotlinx.datetime.Month.JUNE -> R.string.month_june
            kotlinx.datetime.Month.JULY -> R.string.month_july
            kotlinx.datetime.Month.AUGUST -> R.string.month_august
            kotlinx.datetime.Month.SEPTEMBER -> R.string.month_september
            kotlinx.datetime.Month.OCTOBER -> R.string.month_october
            kotlinx.datetime.Month.NOVEMBER -> R.string.month_november
            kotlinx.datetime.Month.DECEMBER -> R.string.month_december
            else -> R.string.month_january
        }
    }

    // Non-localized version (fallback)
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

    // Localized version for Composable
    @Composable
    fun formatDateHeaderLocalized(date: LocalDate): String {
        return when (date) {
            today -> stringResource(R.string.today)
            yesterday -> stringResource(R.string.yesterday)
            else -> {
                val dayOfWeek = stringResource(getDayOfWeekResId(date.dayOfWeek))
                val month = stringResource(getMonthResId(date.month))
                "$dayOfWeek, ${date.dayOfMonth} $month ${date.year}"
            }
        }
    }

    fun formatTime(dateTime: LocalDateTime): String {
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    // Non-localized version (fallback)
    fun formatShortDate(date: LocalDate): String {
        val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        return "${date.dayOfMonth} $month"
    }

    // Localized version for Composable
    @Composable
    fun formatShortDateLocalized(date: LocalDate): String {
        val month = stringResource(getMonthResId(date.month)).take(3)
        return "${date.dayOfMonth} $month"
    }

    // Non-localized version (fallback)
    fun formatFullDate(date: LocalDate): String {
        val dayOfWeek = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$dayOfWeek, ${date.dayOfMonth} $month ${date.year}"
    }

    // Localized version for Composable
    @Composable
    fun formatFullDateLocalized(date: LocalDate): String {
        val dayOfWeek = stringResource(getDayOfWeekResId(date.dayOfWeek))
        val month = stringResource(getMonthResId(date.month))
        return "$dayOfWeek, ${date.dayOfMonth} $month ${date.year}"
    }
}
