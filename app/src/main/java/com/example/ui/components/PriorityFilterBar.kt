package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskPriority

/**
 * Polished Material 3 filtering UI at the top of the LazyColumn
 * to allow users to filter tasks by priority level (High, Medium, Low).
 */
@Composable
fun PriorityFilterBar(
    selectedPriority: TaskPriority?,
    onSelectPriority: (TaskPriority?) -> Unit,
    taskCountByPriority: Map<TaskPriority, Int> = emptyMap(),
    totalTaskCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("priority_filter_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title & Active Filter status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Prioritas",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Filter Prioritas / Priority",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // If a priority filter is active, show quick reset action
                if (selectedPriority != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectPriority(null) }
                            .defaultMinSize(minHeight = 32.dp)
                            .testTag("reset_priority_filter")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus Filter Prioritas",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Reset",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Priority Filter Buttons Row (High, Medium, Low, All)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("priority_filter_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ALL (Semua) Button
                val isAllSelected = selectedPriority == null
                PriorityFilterChipItem(
                    label = "All",
                    subLabel = "Semua",
                    count = totalTaskCount,
                    isSelected = isAllSelected,
                    dotColor = MaterialTheme.colorScheme.primary,
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    testTag = "priority_filter_all",
                    onClick = { onSelectPriority(null) }
                )

                // HIGH (Tinggi) Button
                val isHighSelected = selectedPriority == TaskPriority.TINGGI
                val highCount = taskCountByPriority[TaskPriority.TINGGI] ?: 0
                PriorityFilterChipItem(
                    label = "High",
                    subLabel = "Tinggi",
                    count = highCount,
                    isSelected = isHighSelected,
                    dotColor = TaskPriority.TINGGI.color,
                    activeContainerColor = TaskPriority.TINGGI.containerColor,
                    activeContentColor = TaskPriority.TINGGI.onContainerColor,
                    testTag = "priority_filter_high",
                    onClick = {
                        onSelectPriority(if (isHighSelected) null else TaskPriority.TINGGI)
                    }
                )

                // MEDIUM (Sedang) Button
                val isMediumSelected = selectedPriority == TaskPriority.SEDANG
                val mediumCount = taskCountByPriority[TaskPriority.SEDANG] ?: 0
                PriorityFilterChipItem(
                    label = "Medium",
                    subLabel = "Sedang",
                    count = mediumCount,
                    isSelected = isMediumSelected,
                    dotColor = TaskPriority.SEDANG.color,
                    activeContainerColor = TaskPriority.SEDANG.containerColor,
                    activeContentColor = TaskPriority.SEDANG.onContainerColor,
                    testTag = "priority_filter_medium",
                    onClick = {
                        onSelectPriority(if (isMediumSelected) null else TaskPriority.SEDANG)
                    }
                )

                // LOW (Rendah) Button
                val isLowSelected = selectedPriority == TaskPriority.RENDAH
                val lowCount = taskCountByPriority[TaskPriority.RENDAH] ?: 0
                PriorityFilterChipItem(
                    label = "Low",
                    subLabel = "Rendah",
                    count = lowCount,
                    isSelected = isLowSelected,
                    dotColor = TaskPriority.RENDAH.color,
                    activeContainerColor = TaskPriority.RENDAH.containerColor,
                    activeContentColor = TaskPriority.RENDAH.onContainerColor,
                    testTag = "priority_filter_low",
                    onClick = {
                        onSelectPriority(if (isLowSelected) null else TaskPriority.RENDAH)
                    }
                )
            }

            // Informative Filter Status banner when active
            AnimatedVisibility(
                visible = selectedPriority != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (selectedPriority != null) {
                    val activeCount = taskCountByPriority[selectedPriority] ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(selectedPriority.containerColor.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(selectedPriority.color)
                            )
                            Text(
                                text = "Menampilkan prioritas ${selectedPriority.englishName} (${selectedPriority.displayName}): $activeCount tugas",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = selectedPriority.onContainerColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Priority Filter Chip with 48dp minimum touch target
 * and clear visual priority indicator.
 */
@Composable
private fun PriorityFilterChipItem(
    label: String,
    subLabel: String,
    count: Int,
    isSelected: Boolean,
    dotColor: Color,
    activeContainerColor: Color,
    activeContentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        activeContainerColor
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    val contentColor = if (isSelected) {
        activeContentColor
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (isSelected) {
        dotColor
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Visual Priority Indicator Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )

            // Primary Label (High, Medium, Low, All)
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = contentColor
            )

            // SubLabel (Tinggi, Sedang, Rendah)
            Text(
                text = "($subLabel)",
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.8f)
            )

            // Count Pill
            Surface(
                shape = CircleShape,
                color = if (isSelected) dotColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 2.dp)
            ) {
                Text(
                    text = "$count",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }

            // Checkmark icon when selected
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
