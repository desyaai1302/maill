package com.example.data.repository

import android.content.Context
import com.example.data.local.TaskDao
import com.example.data.model.SubTask
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.notification.TaskNotificationHelper
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class TaskRepository(
    private val taskDao: TaskDao,
    private val context: Context
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getTasksByDateRange(startMillis: Long, endMillis: Long): Flow<List<TaskEntity>> {
        return taskDao.getTasksByDateRange(startMillis, endMillis)
    }

    suspend fun getTaskById(id: Long): TaskEntity? {
        return taskDao.getTaskByIdDirect(id)
    }

    suspend fun insertTask(task: TaskEntity): Long {
        val newId = taskDao.insertTask(task)
        val insertedTask = task.copy(id = newId)
        if (insertedTask.hasReminder && insertedTask.reminderEpochMillis != null) {
            TaskNotificationHelper.scheduleTaskReminder(context, insertedTask)
        }
        return newId
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
        if (task.hasReminder && task.reminderEpochMillis != null && !task.isCompleted) {
            TaskNotificationHelper.scheduleTaskReminder(context, task)
        } else {
            TaskNotificationHelper.cancelTaskReminder(context, task.id)
        }
    }

    suspend fun deleteTask(task: TaskEntity) {
        TaskNotificationHelper.cancelTaskReminder(context, task.id)
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) {
        TaskNotificationHelper.cancelTaskReminder(context, id)
        taskDao.deleteTaskById(id)
    }

    suspend fun toggleTaskCompleted(id: Long, currentCompleted: Boolean) {
        val newStatus = !currentCompleted
        taskDao.setTaskCompleted(id, newStatus)
        if (newStatus) {
            TaskNotificationHelper.cancelTaskReminder(context, id)
        } else {
            val task = taskDao.getTaskByIdDirect(id)
            if (task != null && task.hasReminder && task.reminderEpochMillis != null) {
                TaskNotificationHelper.scheduleTaskReminder(context, task)
            }
        }
    }

    suspend fun toggleSubTask(taskId: Long, subTaskId: String) {
        val task = taskDao.getTaskByIdDirect(taskId) ?: return
        val currentSubTasks = task.parseSubTasks()
        val updatedSubTasks = currentSubTasks.map {
            if (it.id == subTaskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        val updatedTask = task.copy(
            subTasksJson = TaskEntity.encodeSubTasks(updatedSubTasks),
            updatedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updatedTask)
    }

    suspend fun exportTasksAsJson(tasks: List<TaskEntity>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "TugasQu")
        root.put("exportTimestamp", System.currentTimeMillis())

        val array = JSONArray()
        tasks.forEach { task ->
            val obj = JSONObject()
            obj.put("title", task.title)
            obj.put("description", task.description)
            obj.put("priority", task.priority.name)
            obj.put("category", task.category.name)
            obj.put("dueDateMillis", task.dueDateMillis)
            obj.put("dueTimeHour", task.dueTimeHour)
            obj.put("dueTimeMinute", task.dueTimeMinute)
            obj.put("hasReminder", task.hasReminder)
            obj.put("reminderEpochMillis", task.reminderEpochMillis ?: -1L)
            obj.put("isCompleted", task.isCompleted)
            obj.put("subTasksJson", task.subTasksJson)
            obj.put("isClassTask", task.isClassTask)
            obj.put("classCode", task.classCode)
            obj.put("authorName", task.authorName)
            obj.put("authorRole", task.authorRole)
            obj.put("createdAt", task.createdAt)
            array.put(obj)
        }
        root.put("tasks", array)
        return root.toString(2)
    }

    suspend fun importTasksFromJson(jsonString: String): Result<Int> {
        return try {
            val root = JSONObject(jsonString)
            val array = root.getJSONArray("tasks")
            val list = mutableListOf<TaskEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val reminderMillis = obj.optLong("reminderEpochMillis", -1L)
                val task = TaskEntity(
                    id = 0,
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    priority = TaskPriority.fromString(obj.optString("priority", "SEDANG")),
                    category = TaskCategory.fromString(obj.optString("category", "PEKERJAAN")),
                    dueDateMillis = obj.getLong("dueDateMillis"),
                    dueTimeHour = obj.optInt("dueTimeHour", -1),
                    dueTimeMinute = obj.optInt("dueTimeMinute", -1),
                    hasReminder = obj.optBoolean("hasReminder", false),
                    reminderEpochMillis = if (reminderMillis > 0) reminderMillis else null,
                    isCompleted = obj.optBoolean("isCompleted", false),
                    subTasksJson = obj.optString("subTasksJson", "[]"),
                    isClassTask = obj.optBoolean("isClassTask", false),
                    classCode = obj.optString("classCode", ""),
                    authorName = obj.optString("authorName", ""),
                    authorRole = obj.optString("authorRole", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = System.currentTimeMillis()
                )
                list.add(task)
            }
            if (list.isNotEmpty()) {
                taskDao.insertAll(list)
            }
            Result.success(list.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
