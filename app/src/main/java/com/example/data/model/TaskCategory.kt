package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

enum class TaskCategory(
    val displayName: String,
    val icon: ImageVector
) {
    PEKERJAAN("Pekerjaan", Icons.Default.Work),
    PRIBADI("Pribadi", Icons.Default.Person),
    BELAJAR("Belajar", Icons.Default.School),
    KESEHATAN("Kesehatan", Icons.Default.FitnessCenter),
    KEUANGAN("Keuangan", Icons.Default.AccountBalanceWallet),
    BELANJA("Belanja", Icons.Default.ShoppingCart),
    LAINNYA("Lainnya", Icons.Default.Category);

    companion object {
        fun fromString(value: String): TaskCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: LAINNYA
        }
    }
}
