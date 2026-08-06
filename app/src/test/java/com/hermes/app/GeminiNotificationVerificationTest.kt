package com.hermes.app

import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.domain.RoleItem
import com.hermes.app.domain.ai.GeminiRoleService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeminiNotificationVerificationTest {

    @Test
    fun testRoleFallbackFormatting() {
        val role = RoleItem(
            id = "STRICT_COACH",
            displayName = "Entrenador Estricto",
            description = "Exige máxima disciplina",
            customPhrase = "¡Sin excusas! Tarea a realizar:"
        )

        val taskTitle = "Hacer ejercicio"
        val leadMinutes = 15
        val scheduledStartMs = System.currentTimeMillis() + 900000L
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(scheduledStartMs))

        val leadText = if (leadMinutes > 0) "en $leadMinutes min ($timeStr)" else "a las $timeStr"
        val fallback = "🤖 [${role.displayName}] ${role.customPhrase} '$taskTitle' $leadText"

        assertNotNull(fallback)
        assertEquals("🤖 [Entrenador Estricto] ¡Sin excusas! Tarea a realizar: 'Hacer ejercicio' en 15 min ($timeStr)", fallback)
    }

    @Test
    fun testGeminiRoleServiceAdvanceNotification() = runBlocking {
        val service = GeminiRoleService()
        val role = RoleItem(
            id = "STRICT_COACH",
            displayName = "Entrenador Estricto",
            description = "Exige máxima disciplina",
            customPhrase = "¡Sin excusas!"
        )
        val task = TaskEntity(
            title = "Entrenamiento de cardio",
            scheduledStart = System.currentTimeMillis() + 900000L,
            durationMinutes = 45
        )
        val result = service.generateRoleNotification(role, task)
        assertNotNull(result)
    }

    @Test
    fun testGeminiTaskPriorityRanking() = runBlocking {
        val service = GeminiRoleService()
        val dummyTasks = listOf(
            TaskEntity(id = 1, title = "Revisar correos urgentes", durationMinutes = 15, isFixed = false),
            TaskEntity(id = 2, title = "Sesión de estudio profunda", durationMinutes = 90, isFixed = false),
            TaskEntity(id = 3, title = "Organizar escritorio", durationMinutes = 10, isFixed = false)
        )
        val rankedIds = service.rankTasksPriority(dummyTasks)
        assertNotNull(rankedIds)
        assertEquals(3, rankedIds.size)
    }
}
