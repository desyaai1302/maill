package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserProfile
import com.example.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassManagementDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onCreateClass: (className: String, classCode: String, creatorName: String, leaderPin: String) -> Unit,
    onJoinClass: (classCode: String, studentName: String, isLeader: Boolean) -> Unit,
    onAddAuthorizedLeader: (nameOrNim: String) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onUpdateLeaderPin: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Info & Bagikan, 1 = Gabung / Buat, 2 = Akses Ketua

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Ikon Kelas",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Kelola & Masuk Kelas",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sistem Kelas Tanpa Login",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_class_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Active Class Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = userProfile.className,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kode: ${userProfile.classCode}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Role badge
                        Surface(
                            color = if (userProfile.isKetuaKelas) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (userProfile.isKetuaKelas) Icons.Default.Security else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (userProfile.isKetuaKelas) "Ketua Kelas" else "Mahasiswa",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Selector
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Bagikan Link", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Ganti/Buat", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Akses Ketua", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab 0: Bagikan Link & Kode
                if (selectedTab == 0) {
                    Column {
                        Text(
                            text = "Ajak Mahasiswa Bergabung",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mahasiswa lain dapat langsung masuk kelas ini menggunakan Kode Kelas atau Tautan Undangan tanpa perlu login.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "KODE KELAS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = userProfile.classCode,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Button(
                                        onClick = {
                                            copyToClipboard(context, userProfile.classCode, "Kode kelas berhasil disalin!")
                                        },
                                        modifier = Modifier.testTag("copy_class_code_btn")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Salin Kode")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val inviteLink = "tugasqu://join?code=${userProfile.classCode}"
                        val inviteMessage = "Halo teman-teman! Yuk gabung ke kelas ${userProfile.className} di aplikasi TugasQu untuk melihat dan mengumpulkan tugas bersama.\n\nKode Kelas: ${userProfile.classCode}\nTautan Undangan: $inviteLink"

                        OutlinedButton(
                            onClick = {
                                shareText(context, inviteMessage, "Undangan Kelas ${userProfile.className}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_invite_link_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bagikan Tautan Undangan Mahasiswa")
                        }

                        if (userProfile.isKetuaKelas) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val leaderInvite = "tugasqu://join?code=${userProfile.classCode}&leader=true"
                            val leaderMsg = "Undangan Akses Ketua Kelas untuk ${userProfile.className}.\nGunakan kode kelas ${userProfile.classCode} dengan PIN ${userProfile.leaderPin} atau klik link berikut: $leaderInvite"

                            OutlinedButton(
                                onClick = {
                                    shareText(context, leaderMsg, "Akses Ketua Kelas ${userProfile.className}")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("share_leader_invite_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bagikan Tautan Akses Ketua Kelas")
                            }
                        }
                    }
                }

                // Tab 1: Ganti Kelas atau Buat Kelas
                if (selectedTab == 1) {
                    Column {
                        var isCreatingNew by remember { mutableStateOf(false) }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { isCreatingNew = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isCreatingNew) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (!isCreatingNew) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text("Gabung Kelas")
                            }
                            Button(
                                onClick = { isCreatingNew = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCreatingNew) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isCreatingNew) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text("+ Buat Baru")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isCreatingNew) {
                            // Join by Code
                            var inputCode by remember { mutableStateOf("") }
                            var inputName by remember { mutableStateOf(userProfile.name) }
                            var inputPin by remember { mutableStateOf("") }

                            Text(
                                text = "Masuk Kelas dengan Kode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Masukkan kode kelas yang dibagikan oleh Ketua Kelas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = inputCode,
                                onValueChange = { inputCode = it.uppercase() },
                                label = { Text("Kode Kelas (Contoh: TI-2026A)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_join_code"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = inputName,
                                onValueChange = { inputName = it },
                                label = { Text("Nama Anda") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = inputPin,
                                onValueChange = { inputPin = it },
                                label = { Text("PIN Akses Ketua (Opsional, jika diberi akses)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (inputCode.isNotBlank()) {
                                        val isLeaderAccess = inputPin.isNotBlank() && inputPin == userProfile.leaderPin
                                        onJoinClass(inputCode, inputName, isLeaderAccess)
                                        Toast.makeText(context, "Berhasil masuk kelas $inputCode!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Harap masukkan kode kelas!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_join_class_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Masuk Kelas Sekarang")
                            }
                        } else {
                            // Create New Class (Automatic Ketua Kelas)
                            var newClassName by remember { mutableStateOf("") }
                            var newClassCode by remember { mutableStateOf("") }
                            var creatorName by remember { mutableStateOf(userProfile.name) }
                            var newLeaderPin by remember { mutableStateOf("1234") }

                            Text(
                                text = "Buat Kelas Baru",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Orang yang membuat kelas ini otomatis menjadi Ketua Kelas dan dapat mengelola serta memberikan akses ketua ke anggota lain.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = newClassName,
                                onValueChange = { newClassName = it },
                                label = { Text("Nama Kelas (Contoh: Sistem Informasi 2026)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_new_class_name"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newClassCode,
                                onValueChange = { newClassCode = it.uppercase() },
                                label = { Text("Kode Kelas (Contoh: SI-2026)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_new_class_code"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = creatorName,
                                onValueChange = { creatorName = it },
                                label = { Text("Nama Anda (Sebagai Ketua Kelas)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newLeaderPin,
                                onValueChange = { newLeaderPin = it },
                                label = { Text("PIN Akses Ketua (Untuk delegasi anggota lain)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (newClassName.isNotBlank() && newClassCode.isNotBlank()) {
                                        onCreateClass(newClassName, newClassCode, creatorName, newLeaderPin)
                                        Toast.makeText(context, "Kelas $newClassName berhasil dibuat! Anda sekarang Ketua Kelas.", Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Harap isi Nama dan Kode Kelas!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_create_class_btn")
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Buat Kelas & Jadi Ketua Kelas")
                            }
                        }
                    }
                }

                // Tab 2: Akses Ketua Kelas & Delegasi
                if (selectedTab == 2) {
                    Column {
                        Text(
                            text = "Kelola Akses Ketua Kelas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ketua Kelas dapat menambahkan anggota lain untuk ikut mengakses fitur Ketua Kelas (membuat tugas sekaligus untuk semua mahasiswa).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Role toggle
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Beralih Peran Langsung",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Uji tampilan aplikasi sebagai Ketua Kelas atau Mahasiswa biasa.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onSwitchRole(UserRole.MAHASISWA)
                                            Toast.makeText(context, "Peran beralih ke Mahasiswa", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = if (!userProfile.isKetuaKelas) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else ButtonDefaults.outlinedButtonColors()
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mahasiswa")
                                    }

                                    Button(
                                        onClick = {
                                            onSwitchRole(UserRole.KETUA_KELAS)
                                            Toast.makeText(context, "Peran beralih ke Ketua Kelas!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = if (userProfile.isKetuaKelas) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ketua Kelas")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Add authorized member
                        var memberNameOrNim by remember { mutableStateOf("") }

                        Text(
                            text = "Beri Akses Ketua ke Anggota Lain",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = memberNameOrNim,
                                onValueChange = { memberNameOrNim = it },
                                label = { Text("Nama atau NIM Anggota") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_authorized_member"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (memberNameOrNim.isNotBlank()) {
                                        onAddAuthorizedLeader(memberNameOrNim)
                                        Toast.makeText(context, "$memberNameOrNim diberikan akses Ketua Kelas!", Toast.LENGTH_SHORT).show()
                                        memberNameOrNim = ""
                                    }
                                },
                                modifier = Modifier.testTag("add_authorized_member_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Authorized members list
                        Text(
                            text = "Daftar Pemegang Akses Ketua Kelas:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        userProfile.authorizedLeaders.forEach { leader ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(leader, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Edit PIN
                        var pinInput by remember { mutableStateOf(userProfile.leaderPin) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("PIN Akses Ketua") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    if (pinInput.isNotBlank()) {
                                        onUpdateLeaderPin(pinInput)
                                        Toast.makeText(context, "PIN diperbarui ke $pinInput", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Simpan PIN")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, message: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Kode Kelas", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String, title: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, title))
}
