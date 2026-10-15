package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.SubTask
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.model.UserRole
import com.example.data.repository.TaskRepository
import com.example.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TugasQu", appName)
    }

    @Test
    fun `task entity encode and parse subtasks correctly`() {
        val subTasks = listOf(
            SubTask(id = "1", title = "Langkah 1", isCompleted = false),
            SubTask(id = "2", title = "Langkah 2", isCompleted = true)
        )
        val json = TaskEntity.encodeSubTasks(subTasks)
        val task = TaskEntity(
            title = "Test Task",
            priority = TaskPriority.TINGGI,
            dueDateMillis = System.currentTimeMillis(),
            subTasksJson = json,
            isClassTask = true,
            classCode = "TI-2026A"
        )

        val parsed = task.parseSubTasks()
        assertEquals(2, parsed.size)
        assertEquals("Langkah 1", parsed[0].title)
        assertFalse(parsed[0].isCompleted)
        assertEquals("Langkah 2", parsed[1].title)
        assertTrue(parsed[1].isCompleted)
        assertTrue(task.isClassTask)
        assertEquals("TI-2026A", task.classCode)
    }

    @Test
    fun `task repository operations work correctly`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val repository = TaskRepository(db.taskDao(), context)

        val task = TaskEntity(
            title = "Tugas Repository Test",
            description = "Deskripsi tugas",
            priority = TaskPriority.TINGGI,
            dueDateMillis = System.currentTimeMillis()
        )

        val id = repository.insertTask(task)
        assertTrue(id > 0)

        val fetched = repository.getTaskById(id)
        assertEquals("Tugas Repository Test", fetched?.title)
        assertFalse(fetched!!.isCompleted)

        repository.toggleTaskCompleted(id, false)
        val updated = repository.getTaskById(id)
        assertTrue(updated!!.isCompleted)

        val allList = repository.allTasks.first()
        assertTrue(allList.any { it.id == id && it.isCompleted })

        repository.deleteTaskById(id)
        val deleted = repository.getTaskById(id)
        assertTrue(deleted == null)
    }

    @Test
    fun `ketua kelas login and role verification`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = TaskViewModel(app)

        viewModel.login(
            name = "Rian Hidayat",
            nim = "220101005",
            classCode = "TI-2026A",
            role = UserRole.KETUA_KELAS
        )

        val user = viewModel.currentUser.value
        assertTrue(user.isLoggedIn)
        assertTrue(user.isKetuaKelas)
        assertEquals("Rian Hidayat", user.name)
        assertEquals("TI-2026A", user.classCode)
        assertEquals(UserRole.KETUA_KELAS, user.role)
    }

    @Test
    fun `openAppDirectly provides immediate access without crash`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = TaskViewModel(app)

        viewModel.openAppDirectly(UserRole.MAHASISWA)

        val user = viewModel.currentUser.value
        assertTrue(user.isLoggedIn)
        assertFalse(user.isKetuaKelas)
        assertEquals(UserRole.MAHASISWA, user.role)
        assertEquals("TI-2026A", user.classCode)
    }
}
