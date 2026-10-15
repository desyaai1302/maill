package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.notification.TaskNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val upcomingTasks = db.taskDao().getUpcomingReminders(System.currentTimeMillis())
                    for (task in upcomingTasks) {
                        TaskNotificationHelper.scheduleTaskReminder(context, task)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
