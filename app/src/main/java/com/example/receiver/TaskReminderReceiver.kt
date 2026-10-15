package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.notification.TaskNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TaskNotificationHelper.ACTION_REMINDER -> {
                val taskId = intent.getLongExtra(TaskNotificationHelper.EXTRA_TASK_ID, -1L)
                val title = intent.getStringExtra(TaskNotificationHelper.EXTRA_TASK_TITLE) ?: "Tugas Anda"
                val priority = intent.getStringExtra(TaskNotificationHelper.EXTRA_TASK_PRIORITY) ?: "Sedang"
                val category = intent.getStringExtra(TaskNotificationHelper.EXTRA_TASK_CATEGORY) ?: "Umum"

                if (taskId != -1L) {
                    // Check if task is still pending before notifying
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val task = db.taskDao().getTaskByIdDirect(taskId)
                            if (task != null && !task.isCompleted) {
                                TaskNotificationHelper.showNotification(
                                    context = context,
                                    taskId = taskId,
                                    title = title,
                                    priority = priority,
                                    category = category
                                )
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            TaskNotificationHelper.ACTION_MARK_COMPLETED -> {
                val taskId = intent.getLongExtra(TaskNotificationHelper.EXTRA_TASK_ID, -1L)
                if (taskId != -1L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            db.taskDao().setTaskCompleted(taskId, true)
                            // Dismiss notification
                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                            notificationManager.cancel(taskId.toInt())
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }
    }
}
