package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SubTask
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.util.CalendarIntegrationHelper
import com.example.util.DateTimeUtils
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskEditDialog(
    initialTask: TaskEntity? = null,
    defaultDateMillis: Long = DateTimeUtils.getStartOfDay(),
    isKetuaKelas: Boolean = false,
    classCode: String = "",
    onDismiss: () -> Unit,
    onSaveTask: (
        id: Long,
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
        isClassTask: Boolean
    ) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: TaskPriority.SEDANG) }
    var category by remember { mutableStateOf(initialTask?.category ?: TaskCategory.BELAJAR) }
    var dueDateMillis by remember { mutableStateOf(initialTask?.dueDateMillis ?: defaultDateMillis) }
    var dueHour by remember { mutableStateOf(initialTask?.dueTimeHour ?: 9) }
    var dueMinute by remember { mutableStateOf(initialTask?.dueTimeMinute ?: 0) }
    var hasTime by remember { mutableStateOf(initialTask?.dueTimeHour != null && initialTask.dueTimeHour >= 0) }
    var hasReminder by remember { mutableStateOf(initialTask?.hasReminder ?: true) }
    var isClassTask by remember { mutableStateOf(initialTask?.isClassTask ?: isKetuaKelas) }

    val subTasks = remember {
        mutableStateListOf<SubTask>().apply {
            if (initialTask != null) {
                addAll(initialTask.parseSubTasks())
            }
        }
    }
    var newSubTaskText by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }

    // DatePicker dialog
    val calendar = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                dueDateMillis = newCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // TimePicker dialog
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minuteOfHour ->
                dueHour = hourOfDay
                dueMinute = minuteOfHour
                hasTime = true
            },
            if (dueHour >= 0) dueHour else 9,
            if (dueMinute >= 0) dueMinute else 0,
            true
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTask == null) {
                            if (isKetuaKelas) "Buat Tugas Kelas / Pribadi" else "Tambah Tugas Baru"
                        } else "Edit Tugas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Ketua Kelas Broadcast Toggle Banner
                if (isKetuaKelas) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isClassTask) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = if (isClassTask) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isClassTask) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Tugas Kelas (Broadcast Semua Mahasiswa)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isClassTask) Color(0xFF78350F) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Tugas akan otomatis terdistribusi ke seluruh mahasiswa kelas $classCode",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isClassTask) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isClassTask,
                                onCheckedChange = { isClassTask = it }
                            )
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Judul Tugas *") },
                    placeholder = { Text(if (isClassTask) "Contoh: Tugas 3 - Analisis Basis Data Relasional" else "Contoh: Selesaikan Tugas Mandiri") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) Text("Judul tugas wajib diisi")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi / Petunjuk Pengerjaan (Opsional)") },
                    placeholder = { Text("Catat format pengumpulan, tautan materi kuliah, atau batas instruksi...") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input")
                )

                // Priority Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tingkat Prioritas",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskPriority.entries.forEach { p ->
                            val isSelected = priority == p
                            FilterChip(
                                selected = isSelected,
                                onClick = { priority = p },
                                label = {
                                    Text(
                                        text = p.displayName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = p.containerColor,
                                    selectedLabelColor = p.onContainerColor
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Category Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Kategori Tugas",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskCategory.entries.forEach { cat ->
                            val isSelected = category == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                leadingIcon = {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(cat.displayName, fontSize = 12.sp) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Date & Time Scheduling
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Jadwal & Batas Waktu",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Date Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = DateTimeUtils.formatFullDate(dueDateMillis),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            TextButton(onClick = { datePickerDialog.show() }) {
                                Text("Ubah")
                            }
                        }

                        // Time Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (hasTime) "Pukul ${DateTimeUtils.formatTime(dueHour, dueMinute)}" else "Sepanjang Hari",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (hasTime) {
                                    TextButton(onClick = { timePickerDialog.show() }) {
                                        Text("Atur Jam")
                                    }
                                }
                                Switch(
                                    checked = hasTime,
                                    onCheckedChange = { checked ->
                                        hasTime = checked
                                        if (checked) timePickerDialog.show()
                                    }
                                )
                            }
                        }

                        // Notification Reminder Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Pengingat Notifikasi",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Alarm & banner saat waktu tiba",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = hasReminder,
                                onCheckedChange = { hasReminder = it }
                            )
                        }
                    }
                }

                // Subtasks checklist builder
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Daftar Sub-tugas (${subTasks.size})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    subTasks.forEachIndexed { index, st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}. ${st.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { subTasks.removeAt(index) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus Sub-tugas",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Input to add subtask
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubTaskText,
                            onValueChange = { newSubTaskText = it },
                            placeholder = { Text("Tambah langkah/sub-tugas...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newSubTaskText.isNotBlank()) {
                                    subTasks.add(SubTask(title = newSubTaskText.trim()))
                                    newSubTaskText = ""
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Sub-tugas",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Direct System Calendar Integration Button
                OutlinedButton(
                    onClick = {
                        val tempTask = TaskEntity(
                            title = if (title.isBlank()) "Tugas Baru" else title,
                            description = description,
                            priority = priority,
                            category = category,
                            dueDateMillis = dueDateMillis,
                            dueTimeHour = if (hasTime) dueHour else -1,
                            dueTimeMinute = if (hasTime) dueMinute else -1,
                            isClassTask = isClassTask,
                            classCode = if (isClassTask) classCode else ""
                        )
                        CalendarIntegrationHelper.addToSystemCalendar(context, tempTask)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sinkronkan ke Kalender Google/Android")
                }

                // Action Buttons: Batal & Simpan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                                return@Button
                            }

                            val reminderTime = if (hasReminder) {
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = dueDateMillis
                                    if (hasTime) {
                                        set(Calendar.HOUR_OF_DAY, dueHour)
                                        set(Calendar.MINUTE, dueMinute)
                                        set(Calendar.SECOND, 0)
                                    } else {
                                        set(Calendar.HOUR_OF_DAY, 9)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }
                                }
                                cal.timeInMillis
                            } else null

                            onSaveTask(
                                initialTask?.id ?: 0L,
                                title.trim(),
                                description.trim(),
                                priority,
                                category,
                                dueDateMillis,
                                if (hasTime) dueHour else -1,
                                if (hasTime) dueMinute else -1,
                                hasReminder,
                                reminderTime,
                                subTasks.toList(),
                                isClassTask
                            )
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text(if (isClassTask) "Simpan & Broadcast Tugas" else "Simpan Tugas")
                    }
                }
            }
        }
    }
}
