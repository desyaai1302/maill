package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import com.example.data.model.TaskEntity
import java.util.Calendar

object CalendarIntegrationHelper {
    /**
     * Opens system Calendar (e.g. Google Calendar) with task details pre-filled.
     * Uses Intent.ACTION_INSERT, which requires NO dangerous permissions and lets user
     * review and save the event directly into their synced Google Account calendar.
     */
    fun addToSystemCalendar(context: Context, task: TaskEntity) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = task.dueDateMillis
            if (task.dueTimeHour >= 0 && task.dueTimeMinute >= 0) {
                set(Calendar.HOUR_OF_DAY, task.dueTimeHour)
                set(Calendar.MINUTE, task.dueTimeMinute)
                set(Calendar.SECOND, 0)
            } else {
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
        }
        val startMillis = cal.timeInMillis
        val endMillis = startMillis + (60 * 60 * 1000) // 1 hour duration default

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, task.title)
            putExtra(
                CalendarContract.Events.DESCRIPTION,
                "${task.description}\n\n[Kategori: ${task.category.displayName} | Prioritas: ${task.priority.displayName} via TugasQu]"
            )
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.ALL_DAY, task.dueTimeHour < 0)
            putExtra(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "Tidak dapat membuka aplikasi kalender",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
