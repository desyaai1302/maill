package com.example.data.model

data class UserProfile(
    val id: String = "user_default",
    val name: String = "Mahasiswa",
    val nim: String = "220101001",
    val classCode: String = "TI-2026A",
    val className: String = "Teknik Informatika 2026-A",
    val role: UserRole = UserRole.MAHASISWA,
    val leaderPin: String = "1234",
    val authorizedLeaders: Set<String> = emptySet(),
    val isLoggedIn: Boolean = true // Always active so app opens directly without login barrier
) {
    val isKetuaKelas: Boolean get() = role == UserRole.KETUA_KELAS
}
