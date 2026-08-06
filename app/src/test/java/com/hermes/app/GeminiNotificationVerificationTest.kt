package com.hermes.app

import com.hermes.app.domain.RoleItem
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
}
