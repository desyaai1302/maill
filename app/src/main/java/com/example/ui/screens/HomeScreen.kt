package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.components.CalendarView
import com.example.ui.components.ClassManagementDialog
import com.example.ui.components.PriorityFilterBar
import com.example.ui.components.ProductivityDashboard
import com.example.ui.components.SyncBackupDialog
import com.example.ui.components.TaskCard
import com.example.ui.components.TaskEditDialog
import com.example.ui.components.TaskFilterChips
import com.example.ui.components.ThemeSettingDialog
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.ProductivityStats
import com.example.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val stats by viewModel.productivityStats.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedPriority by viewModel.selectedPriorityFilter.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val completionFilter by viewModel.completionFilter.collectAsStateWithLifecycle()
    val classTaskOnlyFilter by viewModel.classTaskOnlyFilter.collectAsStateWithLifecycle()
    val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Tugas, 1 = Kalender, 2 = Profil & Kelas
    var showTaskEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showClassDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var dateForNewTask by remember { mutableStateOf<Long?>(null) }
    var isCreatingBroadcastTask by remember { mutableStateOf(false) }

    // Request Notification permission on Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TugasQu",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Clickable Role Badge (Ketua Kelas / Mahasiswa) in App Bar
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentUser.isKetuaKelas) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showClassDialog = true }
                                    .testTag("appbar_role_badge")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = if (currentUser.isKetuaKelas) Icons.Default.Security else Icons.Default.School,
                                        contentDescription = null,
                                        tint = if (currentUser.isKetuaKelas) Color(0xFFD97706) else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentUser.isKetuaKelas) "Ketua Kelas" else "Mahasiswa",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentUser.isKetuaKelas) Color(0xFF78350F) else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        // Clickable class subtitle to open class management
                        Text(
                            text = "${currentUser.name} • Kelas ${currentUser.classCode} (Ketuk untuk kelola)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showClassDialog = true }
                        )
                    }
                },
                actions = {
                    // Class Management & Invite Action Button
                    IconButton(
                        onClick = { showClassDialog = true },
                        modifier = Modifier.testTag("class_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Kelola & Bagikan Kelas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Theme Dialog Trigger
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.DARK -> Icons.Default.DarkMode
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.SYSTEM -> Icons.Default.DarkMode
                            },
                            contentDescription = "Ubah Tema Tampilan",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Sync & Backup Button
                    IconButton(
                        onClick = { showSyncDialog = true },
                        modifier = Modifier.testTag("sync_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sinkronisasi Antar Perangkat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = "Daftar Tugas"
                        )
                    },
                    label = { Text("Tugas") },
                    modifier = Modifier.testTag("nav_tab_tasks")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Kalender Jadwal"
                        )
                    },
                    label = { Text("Kalender") },
                    modifier = Modifier.testTag("nav_tab_calendar")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil & Kelas"
                        )
                    },
                    label = { Text("Kelas & Profil") },
                    modifier = Modifier.testTag("nav_tab_stats")
                )
            }
        },
        floatingActionButton = {
            if (currentUser.isKetuaKelas) {
                ExtendedFloatingActionButton(
                    onClick = {
                        taskToEdit = null
                        dateForNewTask = null
                        isCreatingBroadcastTask = true
                        showTaskEditDialog = true
                    },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                    text = { Text("Buat Tugas Kelas", fontWeight = FontWeight.Bold) },
                    containerColor = Color(0xFFD97706),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_create_class_task")
                )
            } else {
                FloatingActionButton(
                    onClick = {
                        taskToEdit = null
                        dateForNewTask = null
                        isCreatingBroadcastTask = false
                        showTaskEditDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_task")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Tugas Pribadi"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: Main Screen Layout with LazyColumn
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .testTag("tasks_lazy_column"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Priority Filtering UI at the top of the LazyColumn (High, Medium, Low)
                        item {
                            PriorityFilterBar(
                                selectedPriority = selectedPriority,
                                onSelectPriority = { viewModel.setPriorityFilter(it) },
                                taskCountByPriority = mapOf(
                                    TaskPriority.TINGGI to allTasks.count { it.priority == TaskPriority.TINGGI },
                                    TaskPriority.SEDANG to allTasks.count { it.priority == TaskPriority.SEDANG },
                                    TaskPriority.RENDAH to allTasks.count { it.priority == TaskPriority.RENDAH }
                                ),
                                totalTaskCount = allTasks.size,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Quick Class Bar (Showing current class code & quick invite/manage button)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showClassDialog = true },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentUser.isKetuaKelas) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (currentUser.isKetuaKelas) Icons.Default.Security else Icons.Default.Group,
                                            contentDescription = null,
                                            tint = if (currentUser.isKetuaKelas) Color(0xFFD97706) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = currentUser.className,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (currentUser.isKetuaKelas) Color(0xFF78350F) else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Kode: ${currentUser.classCode} • ${if (currentUser.isKetuaKelas) "Akses: Ketua Kelas" else "Akses: Mahasiswa"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (currentUser.isKetuaKelas) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { showClassDialog = true },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (currentUser.isKetuaKelas) Color(0xFF78350F) else MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text("Kelola", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Special Ketua Kelas Broadcast Banner
                        if (currentUser.isKetuaKelas) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFFBEB)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Campaign,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Panel Ketua Kelas: Buat Tugas Serentak",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF78350F)
                                            )
                                        }

                                        Text(
                                            text = "Tugas yang dibuat di sini otomatis terdaftar sebagai Tugas Kelas untuk semua mahasiswa di kelas ${currentUser.classCode}.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF92400E)
                                        )

                                        Button(
                                            onClick = {
                                                taskToEdit = null
                                                dateForNewTask = null
                                                isCreatingBroadcastTask = true
                                                showTaskEditDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFD97706),
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Buat Tugas untuk Seluruh Mahasiswa Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Productivity Dashboard Summary
                        item {
                            ProductivityDashboard(
                                stats = stats,
                                onOpenSyncDialog = { showSyncDialog = true }
                            )
                        }

                        // Search and Filter Chips
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quick Class Task Toggle Chip
                                    FilterChip(
                                        selected = classTaskOnlyFilter,
                                        onClick = { viewModel.toggleClassTaskOnlyFilter() },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (classTaskOnlyFilter) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Hanya Tugas Kelas Resmi",
                                                fontWeight = if (classTaskOnlyFilter) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFEF3C7),
                                            selectedLabelColor = Color(0xFF78350F)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                TaskFilterChips(
                                    searchQuery = searchQuery,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    selectedPriority = selectedPriority,
                                    onSelectPriority = { viewModel.setPriorityFilter(it) },
                                    selectedCategory = selectedCategory,
                                    onSelectCategory = { viewModel.setCategoryFilter(it) },
                                    completionFilter = completionFilter,
                                    onSelectCompletion = { viewModel.setCompletionFilter(it) }
                                )
                            }
                        }

                        // Task List (LazyColumn items with status checkboxes & priority indicators)
                        if (filteredTasks.isEmpty()) {
                            item {
                                EmptyTaskState(
                                    hasFilter = searchQuery.isNotBlank() || selectedPriority != null || selectedCategory != null || completionFilter != null || classTaskOnlyFilter,
                                    onResetFilter = {
                                        viewModel.setSearchQuery("")
                                        viewModel.setPriorityFilter(null)
                                        viewModel.setCategoryFilter(null)
                                        viewModel.setCompletionFilter(null)
                                        if (classTaskOnlyFilter) viewModel.toggleClassTaskOnlyFilter()
                                    },
                                    onAddNewTask = {
                                        taskToEdit = null
                                        isCreatingBroadcastTask = currentUser.isKetuaKelas
                                        showTaskEditDialog = true
                                    }
                                )
                            }
                        } else {
                            items(filteredTasks, key = { it.id }) { task ->
                                TaskCard(
                                    task = task,
                                    onToggleCompleted = { viewModel.toggleTaskCompletion(task) },
                                    onEdit = {
                                        taskToEdit = task
                                        isCreatingBroadcastTask = task.isClassTask
                                        showTaskEditDialog = true
                                    },
                                    onDelete = { viewModel.deleteTask(task) },
                                    onToggleSubTask = { subTaskId ->
                                        viewModel.toggleSubTask(task.id, subTaskId)
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }

                1 -> {
                    // TAB 1: Calendar View
                    CalendarView(
                        allTasks = allTasks,
                        selectedDateMillis = selectedCalendarDate,
                        onSelectDate = { viewModel.setSelectedCalendarDate(it) },
                        onAddNewTaskForDate = { dateMillis ->
                            taskToEdit = null
                            dateForNewTask = dateMillis
                            isCreatingBroadcastTask = currentUser.isKetuaKelas
                            showTaskEditDialog = true
                        },
                        onToggleCompleted = { viewModel.toggleTaskCompletion(it) },
                        onEditTask = {
                            taskToEdit = it
                            isCreatingBroadcastTask = it.isClassTask
                            showTaskEditDialog = true
                        },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onToggleSubTask = { taskId, subTaskId ->
                            viewModel.toggleSubTask(taskId, subTaskId)
                        }
                    )
                }

                2 -> {
                    // TAB 2: Class, Profile & Statistics
                    ProfileAndClassScreenContent(
                        currentUser = currentUser,
                        stats = stats,
                        onOpenClassManagement = { showClassDialog = true },
                        onOpenEditProfile = { showEditProfileDialog = true },
                        onSwitchRole = { role -> viewModel.switchRole(role) },
                        onOpenSync = { showSyncDialog = true }
                    )
                }
            }
        }
    }

    // Class Management Dialog
    if (showClassDialog) {
        ClassManagementDialog(
            userProfile = currentUser,
            onDismiss = { showClassDialog = false },
            onCreateClass = { className, classCode, creatorName, leaderPin ->
                viewModel.createClass(className, classCode, creatorName, leaderPin)
            },
            onJoinClass = { classCode, studentName, isLeader ->
                viewModel.joinClassByCode(classCode, studentName, isLeader)
            },
            onAddAuthorizedLeader = { nameOrNim ->
                viewModel.addAuthorizedLeader(nameOrNim)
            },
            onSwitchRole = { newRole ->
                viewModel.switchRole(newRole)
            },
            onUpdateLeaderPin = { newPin ->
                viewModel.updateLeaderPin(newPin)
            }
        )
    }

    // Edit Name & NIM Dialog
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(currentUser.name) }
        var tempNim by remember { mutableStateOf(currentUser.nim) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Ubah Profil Saya") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Nama Lengkap") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempNim,
                        onValueChange = { tempNim = it },
                        label = { Text("NIM Mahasiswa") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(tempName, tempNim)
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profil diperbarui!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Task Edit / Create Dialog (with Ketua Kelas broadcast support)
    if (showTaskEditDialog) {
        TaskEditDialog(
            initialTask = taskToEdit,
            defaultDateMillis = dateForNewTask ?: selectedCalendarDate,
            isKetuaKelas = currentUser.isKetuaKelas || isCreatingBroadcastTask,
            classCode = currentUser.classCode,
            onDismiss = {
                showTaskEditDialog = false
                isCreatingBroadcastTask = false
            },
            onSaveTask = { id, title, desc, prio, cat, dueMillis, dueH, dueM, hasRem, remMillis, subTasks, isClassTask ->
                viewModel.saveTask(
                    id = id,
                    title = title,
                    description = desc,
                    priority = prio,
                    category = cat,
                    dueDateMillis = dueMillis,
                    dueHour = dueH,
                    dueMinute = dueM,
                    hasReminder = hasRem,
                    reminderEpochMillis = remMillis,
                    subTasks = subTasks,
                    isClassTask = isClassTask || isCreatingBroadcastTask
                )
                showTaskEditDialog = false
                isCreatingBroadcastTask = false
            }
        )
    }

    // Theme Switch Dialog
    if (showThemeDialog) {
        ThemeSettingDialog(
            currentThemeMode = themeMode,
            onSelectThemeMode = { mode ->
                viewModel.setThemeMode(mode)
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Sync & Backup Dialog
    if (showSyncDialog) {
        SyncBackupDialog(
            onDismiss = { showSyncDialog = false },
            onExportData = { viewModel.exportJson() },
            onImportData = { json -> viewModel.importJson(json) }
        )
    }
}

@Composable
private fun ProfileAndClassScreenContent(
    currentUser: UserProfile,
    stats: ProductivityStats,
    onOpenClassManagement: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onOpenSync: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile & Role Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (currentUser.isKetuaKelas) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (currentUser.isKetuaKelas) Icons.Default.Security else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (currentUser.isKetuaKelas) Color(0xFFD97706) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = currentUser.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "NIM: ${currentUser.nim}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${currentUser.className} (${currentUser.classCode})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(onClick = onOpenEditProfile) {
                            Icon(Icons.Default.Edit, contentDescription = "Ubah Nama & NIM")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Role switch and class management buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenClassManagement,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kelola Kelas", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val target = if (currentUser.isKetuaKelas) UserRole.MAHASISWA else UserRole.KETUA_KELAS
                                onSwitchRole(target)
                                Toast.makeText(
                                    context,
                                    "Beralih ke peran ${if (target == UserRole.KETUA_KELAS) "Ketua Kelas" else "Mahasiswa"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (currentUser.isKetuaKelas) "Mode Mahasiswa" else "Mode Ketua", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Quick Share Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bagikan Kelas ke Teman Mahasiswa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ajak mahasiswa lain masuk menggunakan Kode: ${currentUser.classCode} tanpa login.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Kode Kelas", currentUser.classCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Kode kelas ${currentUser.classCode} disalin!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salin Kode")
                        }

                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Ayo gabung kelas ${currentUser.className} di TugasQu! Kode: ${currentUser.classCode}\nTautan: tugasqu://join?code=${currentUser.classCode}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Undangan Kelas"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bagikan")
                        }
                    }
                }
            }
        }

        // Summary Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ringkasan Produktivitas & Tugas Kelas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "Total Tugas", value = stats.totalCount.toString())
                        StatItem(label = "Tugas Kelas", value = stats.classTaskCount.toString())
                        StatItem(label = "Selesai", value = stats.completedCount.toString())
                        StatItem(label = "Tertunda", value = stats.pendingCount.toString())
                    }
                }
            }
        }

        // Backup & Export Button
        item {
            OutlinedButton(
                onClick = onOpenSync,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sinkronisasi, Backup & Ekspor JSON")
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyTaskState(
    hasFilter: Boolean,
    onResetFilter: () -> Unit,
    onAddNewTask: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasFilter) "Tidak Ada Tugas Sesuai Filter" else "Belum Ada Tugas Aktif",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hasFilter) {
                    "Coba reset kata kunci pencarian atau ubah kriteria filter prioritas dan kategori."
                } else {
                    "Hebat! Semua tugas telah selesai atau belum ada tugas baru. Buat tugas baru untuk mulai mengelola jadwal kuliahmu."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (hasFilter) {
                OutlinedButton(
                    onClick = onResetFilter,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reset Filter")
                }
            } else {
                Button(
                    onClick = onAddNewTask,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Tugas Sekarang")
                }
            }
        }
    }
}
