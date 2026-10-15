package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityHighContainer
import com.example.ui.theme.PriorityHighOnContainer
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityLowContainer
import com.example.ui.theme.PriorityLowOnContainer
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.PriorityMediumContainer
import com.example.ui.theme.PriorityMediumOnContainer

enum class TaskPriority(
    val displayName: String,
    val englishName: String,
    val color: Color,
    val containerColor: Color,
    val onContainerColor: Color,
    val sortWeight: Int
) {
    TINGGI(
        displayName = "Tinggi",
        englishName = "High",
        color = PriorityHigh,
        containerColor = PriorityHighContainer,
        onContainerColor = PriorityHighOnContainer,
        sortWeight = 3
    ),
    SEDANG(
        displayName = "Sedang",
        englishName = "Medium",
        color = PriorityMedium,
        containerColor = PriorityMediumContainer,
        onContainerColor = PriorityMediumOnContainer,
        sortWeight = 2
    ),
    RENDAH(
        displayName = "Rendah",
        englishName = "Low",
        color = PriorityLow,
        containerColor = PriorityLowContainer,
        onContainerColor = PriorityLowOnContainer,
        sortWeight = 1
    );

    companion object {
        fun fromString(value: String): TaskPriority {
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true) ||
                it.englishName.equals(value, ignoreCase = true)
            } ?: SEDANG
        }
    }
}
