package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.SubTask
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.preferences.ThemePreferences
import com.example.data.preferences.UserPreferences
import com.example.data.repository.TaskRepository
import com.example.notification.TaskNotificationHelper
import com.example.ui.theme.ThemeMode
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface TaskListUiState {
    data object Loading : TaskListUiState
    data class Success(val tasks: List<TaskEntity>) : TaskListUiState
    data class Error(val message: String) : TaskListUiState
}

data class ProductivityStats(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val highPriorityPending: Int = 0,
    val todayTotal: Int = 0,
    val todayCompleted: Int = 0,
    val classTaskCount: Int = 0,
    val completionPercentage: Int = 0
)

data class TaskFilterCriteria(
    val query: String,
    val priority: TaskPriority?,
    val category: TaskCategory?,
    val completion: Boolean?,
    val classOnly: Boolean
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TaskRepository(database.taskDao(), application)
    private val themePreferences = ThemePreferences(application)
    private val userPreferences = UserPreferences(application)

    init {
        TaskNotificationHelper.createNotificationChannel(application)
        seedSampleClassTaskIfEmpty()
    }

    // User Profile & Authentication State
    val currentUser: StateFlow<UserProfile> = userPreferences.currentUser

    fun createClass(className: String, classCode: String, creatorName: String, leaderPin: String = "1234") {
        userPreferences.createClass(className, classCode, creatorName, leaderPin)
    }

    fun joinClassByCode(classCode: String, studentName: String = "", isLeader: Boolean = false) {
        userPreferences.joinClassByCode(classCode, studentName, isLeader)
    }

    fun addAuthorizedLeader(nameOrNim: String) {
        userPreferences.addAuthorizedLeader(nameOrNim)
    }

    fun switchRole(newRole: UserRole) {
        userPreferences.switchRole(newRole)
    }

    fun updateLeaderPin(newPin: String) {
        userPreferences.updateLeaderPin(newPin)
    }

    fun updateProfile(name: String, nim: String) {
        userPreferences.updateProfile(name, nim)
    }

    fun login(name: String, nim: String, classCode: String, role: UserRole) {
        userPreferences.saveLogin(name, nim, classCode, role)
    }

    fun openAppDirectly(role: UserRole = UserRole.MAHASISWA) {
        userPreferences.openAppDirectly(role)
    }

    fun logout() {
        userPreferences.logout()
    }

    // Theme Mode
    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode

    fun setThemeMode(mode: ThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    /**
     * Flow-based reactive observation of all tasks.
     */
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val taskListUiState: StateFlow<TaskListUiState> = repository.allTasks
        .map<List<TaskEntity>, TaskListUiState> { TaskListUiState.Success(it) }
        .catch { emit(TaskListUiState.Error(it.message ?: "Gagal memuat data tugas")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskListUiState.Loading
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPriorityFilter = MutableStateFlow<TaskPriority?>(null)
    val selectedPriorityFilter: StateFlow<TaskPriority?> = _selectedPriorityFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<TaskCategory?>(null)
    val selectedCategoryFilter: StateFlow<TaskCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _completionFilter = MutableStateFlow<Boolean?>(null)
    val completionFilter: StateFlow<Boolean?> = _completionFilter.asStateFlow()

    private val _classTaskOnlyFilter = MutableStateFlow(false)
    val classTaskOnlyFilter: StateFlow<Boolean> = _classTaskOnlyFilter.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(DateTimeUtils.getStartOfDay())
    val selectedCalendarDate: StateFlow<Long> = _selectedCalendarDate.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        _selectedPriorityFilter.value = priority
    }

    fun setCategoryFilter(category: TaskCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setCompletionFilter(completed: Boolean?) {
        _completionFilter.value = completed
    }

    fun toggleClassTaskOnlyFilter() {
        _classTaskOnlyFilter.value = !_classTaskOnlyFilter.value
    }

    fun setSelectedCalendarDate(dateMillis: Long) {
        _selectedCalendarDate.value = DateTimeUtils.getStartOfDay(dateMillis)
    }

    private val filterParams = combine(
        _searchQuery,
        _selectedPriorityFilter,
        _selectedCategoryFilter,
        _completionFilter,
        _classTaskOnlyFilter
    ) { query, priority, category, completion, classOnly ->
        TaskFilterCriteria(query, priority, category, completion, classOnly)
    }

    /**
     * Combined reactive flow for filtered tasks matching search query, priority, category, and class filter.
     */
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        filterParams
    ) { tasks, criteria ->
        tasks.filter { task ->
            val matchesQuery = criteria.query.isBlank() ||
                    task.title.contains(criteria.query, ignoreCase = true) ||
                    task.description.contains(criteria.query, ignoreCase = true) ||
                    task.authorName.contains(criteria.query, ignoreCase = true)
            val matchesPriority = criteria.priority == null || task.priority == criteria.priority
            val matchesCategory = criteria.category == null || task.category == criteria.category
            val matchesCompletion = criteria.completion == null || task.isCompleted == criteria.completion
            val matchesClassOnly = !criteria.classOnly || task.isClassTask

            matchesQuery && matchesPriority && matchesCategory && matchesCompletion && matchesClassOnly
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val calendarDayTasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        _selectedCalendarDate
    ) { tasks, selectedDate ->
        val selectedDayStart = DateTimeUtils.getStartOfDay(selectedDate)
        tasks.filter { task ->
            DateTimeUtils.getStartOfDay(task.dueDateMillis) == selectedDayStart
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val productivityStats: StateFlow<ProductivityStats> = allTasks.combine(_selectedCalendarDate) { tasks, _ ->
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val pending = total - completed
        val highPriorityPending = tasks.count { !it.isCompleted && it.priority == TaskPriority.TINGGI }
        val classTasks = tasks.count { it.isClassTask }

        val todayStart = DateTimeUtils.getStartOfDay()
        val todayTasks = tasks.filter { DateTimeUtils.getStartOfDay(it.dueDateMillis) == todayStart }
        val todayTotal = todayTasks.size
        val todayCompleted = todayTasks.count { it.isCompleted }

        val percentage = if (total > 0) ((completed.toFloat() / total.toFloat()) * 100).toInt() else 0

        ProductivityStats(
            totalCount = total,
            completedCount = completed,
            pendingCount = pending,
            highPriorityPending = highPriorityPending,
            todayTotal = todayTotal,
            todayCompleted = todayCompleted,
            classTaskCount = classTasks,
            completionPercentage = percentage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductivityStats()
    )

    // ==========================================
    // Task Mutation Methods (Add, Delete, Update)
    // ==========================================

    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun addTask(
        title: String,
        description: String = "",
        priority: TaskPriority = TaskPriority.SEDANG,
        category: TaskCategory = TaskCategory.PEKERJAAN,
        dueDateMillis: Long = System.currentTimeMillis(),
        dueHour: Int = -1,
        dueMinute: Int = -1,
        hasReminder: Boolean = false,
        reminderEpochMillis: Long? = null,
        subTasks: List<SubTask> = emptyList(),
        isClassTask: Boolean = false
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                category = category,
                dueDateMillis = dueDateMillis,
                dueTimeHour = dueHour,
                dueTimeMinute = dueMinute,
                hasReminder = hasReminder,
                reminderEpochMillis = reminderEpochMillis,
                subTasksJson = TaskEntity.encodeSubTasks(subTasks),
                isClassTask = isClassTask,
                classCode = if (isClassTask) user.classCode else "",
                authorName = if (isClassTask) user.name else "",
                authorRole = if (isClassTask) user.role.displayName else "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertTask(task)
        }
    }

    /**
     * Specialized feature for Ketua Kelas:
     * Broadcasts a task simultaneously for all students in the class!
     */
    fun broadcastClassTask(
        title: String,
        description: String,
        priority: TaskPriority,
        category: TaskCategory,
        dueDateMillis: Long,
        dueHour: Int,
        dueMinute: Int,
        hasReminder: Boolean,
        subTasks: List<SubTask>
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val reminderTime = if (hasReminder) {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = dueDateMillis
                    if (dueHour >= 0) {
                        set(Calendar.HOUR_OF_DAY, dueHour)
                        set(Calendar.MINUTE, dueMinute)
                    } else {
                        set(Calendar.HOUR_OF_DAY, 9)
                        set(Calendar.MINUTE, 0)
                    }
                }
                cal.timeInMillis
            } else null

            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                category = category,
                dueDateMillis = dueDateMillis,
                dueTimeHour = dueHour,
                dueTimeMinute = dueMinute,
                hasReminder = hasReminder,
                reminderEpochMillis = reminderTime,
                subTasksJson = TaskEntity.encodeSubTasks(subTasks),
                isClassTask = true,
                classCode = user.classCode.ifBlank { "TI-2026A" },
                authorName = user.name.ifBlank { "Ketua Kelas" },
                authorRole = "Ketua Kelas",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
        }
    }

    fun updateTaskStatus(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            val existing = repository.getTaskById(taskId)
            if (existing != null && existing.isCompleted != isCompleted) {
                repository.toggleTaskCompleted(taskId, existing.isCompleted)
            }
        }
    }

    fun updateTaskStatus(task: TaskEntity, isCompleted: Boolean) {
        updateTaskStatus(task.id, isCompleted)
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task.id, task.isCompleted)
        }
    }

    fun toggleTaskCompletion(taskId: Long) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                repository.toggleTaskCompleted(task.id, task.isCompleted)
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun saveTask(
        id: Long = 0,
        title: String,
        description: String,
        priority: TaskPriority,
        category: TaskCategory,
        dueDateMillis: Long,
        dueHour: Int,
        dueMinute: Int,
        hasReminder: Boolean,
        reminderEpochMillis: Long?,
        subTasks: List<SubTask>,
        isClassTask: Boolean = false
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val task = TaskEntity(
                id = id,
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                category = category,
                dueDateMillis = dueDateMillis,
                dueTimeHour = dueHour,
                dueTimeMinute = dueMinute,
                hasReminder = hasReminder,
                reminderEpochMillis = reminderEpochMillis,
                isCompleted = false,
                subTasksJson = TaskEntity.encodeSubTasks(subTasks),
                isClassTask = isClassTask,
                classCode = if (isClassTask) user.classCode else "",
                authorName = if (isClassTask) user.name else "",
                authorRole = if (isClassTask) user.role.displayName else "",
                createdAt = if (id > 0) {
                    val existing = repository.getTaskById(id)
                    existing?.createdAt ?: System.currentTimeMillis()
                } else System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
        }
    }

    fun toggleSubTask(taskId: Long, subTaskId: String) {
        viewModelScope.launch {
            repository.toggleSubTask(taskId, subTaskId)
        }
    }

    suspend fun exportJson(): String {
        return repository.exportTasksAsJson(allTasks.value)
    }

    suspend fun importJson(jsonContent: String): Result<Int> {
        return repository.importTasksFromJson(jsonContent)
    }

    private fun seedSampleClassTaskIfEmpty() {
        viewModelScope.launch {
            val current = repository.getTaskById(1L)
            if (current == null) {
                val calTomorrow = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                }
                val sampleBroadcast = TaskEntity(
                    id = 0,
                    title = "[TUGAS KELAS] Makalah & Laporan Algoritma Pemrograman",
                    description = "Diumumkan oleh Ketua Kelas: Tugas wajib kelompok semester 3. Kumpulkan file PDF dan tautan repository GitHub sebelum jam 23:59.",
                    priority = TaskPriority.TINGGI,
                    category = TaskCategory.BELAJAR,
                    dueDateMillis = DateTimeUtils.getStartOfDay(calTomorrow.timeInMillis),
                    dueTimeHour = 23,
                    dueTimeMinute = 59,
                    hasReminder = true,
                    reminderEpochMillis = calTomorrow.timeInMillis - (2 * 60 * 60 * 1000), // 2 hours before
                    isCompleted = false,
                    subTasksJson = TaskEntity.encodeSubTasks(
                        listOf(
                            SubTask(id = "1", title = "Analisis Kompleksitas Waktu Algoritma", isCompleted = false),
                            SubTask(id = "2", title = "Implementasi Source Code Kotlin/Java", isCompleted = false),
                            SubTask(id = "3", title = "Penyusunan Format Makalah PDF IEEE", isCompleted = false)
                        )
                    ),
                    isClassTask = true,
                    classCode = "TI-2026A",
                    authorName = "Ahmad Farhan (Ketua Kelas)",
                    authorRole = "Ketua Kelas"
                )
                repository.insertTask(sampleBroadcast)
            }
        }
    }
}
