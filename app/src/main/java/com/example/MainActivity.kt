package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.TugasQuTheme
import com.example.ui.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: TaskViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            // Handle incoming deep link if present (e.g. tugasqu://join?code=TI-2026A)
            handleIncomingIntent(intent, viewModel)

            TugasQuTheme(themeMode = themeMode) {
                HomeScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?, viewModel: TaskViewModel) {
        val data: Uri? = intent?.data
        if (data != null && data.scheme == "tugasqu" && data.host == "join") {
            val classCode = data.getQueryParameter("code")
            val isLeaderInvite = data.getQueryParameter("leader") == "true"
            if (!classCode.isNullOrBlank()) {
                viewModel.joinClassByCode(classCode, isLeader = isLeaderInvite)
                Toast.makeText(
                    this,
                    if (isLeaderInvite) "Bergabung ke kelas $classCode sebagai Ketua Kelas!" else "Bergabung ke kelas $classCode!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
