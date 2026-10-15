package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val localeId = Locale("id", "ID")

    fun getStartOfDay(epochMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun formatDate(millis: Long): String {
        val today = getStartOfDay()
        val taskDay = getStartOfDay(millis)
        val oneDayMillis = 24 * 60 * 60 * 1000L

        return when (taskDay) {
            today -> "Hari Ini"
            today + oneDayMillis -> "Besok"
            today - oneDayMillis -> "Kemarin"
            else -> {
                val format = SimpleDateFormat("dd MMM yyyy", localeId)
                format.format(Date(millis))
            }
        }
    }

    fun formatFullDate(millis: Long): String {
        val format = SimpleDateFormat("EEEE, dd MMMM yyyy", localeId)
        return format.format(Date(millis))
    }

    fun formatMonthYear(millis: Long): String {
        val format = SimpleDateFormat("MMMM yyyy", localeId)
        return format.format(Date(millis))
    }

    fun formatTime(hour: Int, minute: Int): String {
        if (hour < 0 || minute < 0) return ""
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    fun isOverdue(dueDateMillis: Long, dueHour: Int, dueMinute: Int, isCompleted: Boolean): Boolean {
        if (isCompleted) return false
        val now = System.currentTimeMillis()
        val deadlineCal = Calendar.getInstance().apply {
            timeInMillis = dueDateMillis
            if (dueHour >= 0 && dueMinute >= 0) {
                set(Calendar.HOUR_OF_DAY, dueHour)
                set(Calendar.MINUTE, dueMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            } else {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
        }
        return now > deadlineCal.timeInMillis
    }
}
