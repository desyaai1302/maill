package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tugasqu_user_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadUser())
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private fun loadUser(): UserProfile {
        val id = prefs.getString("user_id", UUID.randomUUID().toString()) ?: UUID.randomUUID().toString()
        val name = prefs.getString("user_name", "Mahasiswa") ?: "Mahasiswa"
        val nim = prefs.getString("user_nim", "220101001") ?: "220101001"
        val classCode = prefs.getString("user_class_code", "TI-2026A") ?: "TI-2026A"
        val className = prefs.getString("user_class_name", "Teknik Informatika 2026-A") ?: "Teknik Informatika 2026-A"
        val roleStr = prefs.getString("user_role", UserRole.MAHASISWA.name) ?: UserRole.MAHASISWA.name
        val leaderPin = prefs.getString("leader_pin", "1234") ?: "1234"
        val authorizedLeaders = prefs.getStringSet("authorized_leaders", setOf("Budi Santoso", "220101001")) ?: setOf("Budi Santoso", "220101001")

        val role = try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            UserRole.MAHASISWA
        }

        return UserProfile(
            id = id,
            name = name,
            nim = nim,
            classCode = classCode,
            className = className,
            role = role,
            leaderPin = leaderPin,
            authorizedLeaders = authorizedLeaders,
            isLoggedIn = true
        )
    }

    /**
     * Membuat kelas baru. Pembuat kelas otomatis menjadi Ketua Kelas.
     */
    fun createClass(className: String, classCode: String, creatorName: String, leaderPin: String = "1234") {
        val cleanName = creatorName.trim().ifBlank { "Ketua Kelas" }
        val cleanCode = classCode.trim().uppercase().ifBlank { "TI-2026A" }
        val cleanClassName = className.trim().ifBlank { "Kelas $cleanCode" }
        val cleanPin = leaderPin.trim().ifBlank { "1234" }

        val currentLeaders = prefs.getStringSet("authorized_leaders", emptySet())?.toMutableSet() ?: mutableSetOf()
        currentLeaders.add(cleanName)

        prefs.edit()
            .putString("user_name", cleanName)
            .putString("user_class_code", cleanCode)
            .putString("user_class_name", cleanClassName)
            .putString("user_role", UserRole.KETUA_KELAS.name)
            .putString("leader_pin", cleanPin)
            .putStringSet("authorized_leaders", currentLeaders)
            .apply()

        _currentUser.value = _currentUser.value.copy(
            name = cleanName,
            classCode = cleanCode,
            className = cleanClassName,
            role = UserRole.KETUA_KELAS,
            leaderPin = cleanPin,
            authorizedLeaders = currentLeaders
        )
    }

    /**
     * Gabung kelas menggunakan kode atau invite link.
     */
    fun joinClassByCode(classCode: String, studentName: String = "", isLeader: Boolean = false) {
        val cleanCode = classCode.trim().uppercase().ifBlank { "TI-2026A" }
        val cleanName = studentName.trim().ifBlank { _currentUser.value.name }
        val authorized = _currentUser.value.authorizedLeaders.contains(cleanName) || isLeader
        val resolvedRole = if (authorized) UserRole.KETUA_KELAS else UserRole.MAHASISWA

        val guessedClassName = if (cleanCode.startsWith("TI")) "Teknik Informatika $cleanCode" else "Kelas $cleanCode"

        prefs.edit()
            .putString("user_class_code", cleanCode)
            .putString("user_class_name", guessedClassName)
            .putString("user_role", resolvedRole.name)
            .apply()

        _currentUser.value = _currentUser.value.copy(
            name = cleanName,
            classCode = cleanCode,
            className = guessedClassName,
            role = resolvedRole
        )
    }

    /**
     * Ketua Kelas memberikan hak akses Ketua Kelas ke anggota/mahasiswa lain.
     */
    fun addAuthorizedLeader(nameOrNim: String) {
        val clean = nameOrNim.trim()
        if (clean.isBlank()) return
        val currentLeaders = prefs.getStringSet("authorized_leaders", emptySet())?.toMutableSet() ?: mutableSetOf()
        currentLeaders.add(clean)

        prefs.edit()
            .putStringSet("authorized_leaders", currentLeaders)
            .apply()

        _currentUser.value = _currentUser.value.copy(
            authorizedLeaders = currentLeaders
        )
    }

    /**
     * Mengubah peran langsung (misal untuk pengujian atau beralih mode Ketua / Anggota).
     */
    fun switchRole(newRole: UserRole) {
        prefs.edit()
            .putString("user_role", newRole.name)
            .apply()

        _currentUser.value = _currentUser.value.copy(
            role = newRole
        )
    }

    /**
     * Perbarui PIN Akses Ketua Kelas.
     */
    fun updateLeaderPin(newPin: String) {
        val cleanPin = newPin.trim().ifBlank { "1234" }
        prefs.edit()
            .putString("leader_pin", cleanPin)
            .apply()

        _currentUser.value = _currentUser.value.copy(
            leaderPin = cleanPin
        )
    }

    /**
     * Perbarui profil nama dan NIM pengguna.
     */
    fun updateProfile(name: String, nim: String) {
        val cleanName = name.trim().ifBlank { "Mahasiswa" }
        val cleanNim = nim.trim().ifBlank { "220101001" }

        prefs.edit()
            .putString("user_name", cleanName)
            .putString("user_nim", cleanNim)
            .apply()

        _currentUser.value = _currentUser.value.copy(
            name = cleanName,
            nim = cleanNim
        )
    }

    // Keep saveLogin / openAppDirectly for backward-compat if called
    fun saveLogin(name: String, nim: String, classCode: String, role: UserRole) {
        createClass("Kelas $classCode", classCode, name)
        switchRole(role)
    }

    fun openAppDirectly(role: UserRole = UserRole.MAHASISWA) {
        switchRole(role)
    }

    fun logout() {
        // Just reset to Mahasiswa role without locking screen
        switchRole(UserRole.MAHASISWA)
    }
}
