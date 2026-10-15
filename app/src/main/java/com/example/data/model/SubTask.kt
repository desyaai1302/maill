package com.example.data.model

import java.util.UUID

data class SubTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val isCompleted: Boolean = false
)
