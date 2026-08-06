package com.hermes.app.domain.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.hermes.app.BuildConfig
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.domain.RoleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeminiRoleService {

    // --- CONFIGURACIÓN DEL MODELO ---
    // Usamos el modelo estable más potente de la lista de 2026
    private val MODEL_NAME = "gemini-3.6-flash" 
    
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
        .replace("\"", "")
        .replace("'", "")
        .trim()

    private val generativeModel: GenerativeModel? by lazy {
        val finalKey = apiKey
        if (finalKey.isNotBlank() && finalKey.length > 10) {
            Log.d("GeminiRoleService", "Iniciando motor IA. Modelo: $MODEL_NAME | Clave: ${finalKey.take(4)}...")
            GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = finalKey
            )
        } else {
            Log.e("GeminiRoleService", "API Key no válida. Asegúrate de que empiece por 'AIza' en local.properties")
            null
        }
    }

    /**
     * Genera dinámicamente un mensaje/notificación utilizando Gemini API
     * basándose en la personalidad del rol activo y los detalles de la tarea.
     */
    suspend fun generateRoleNotification(role: RoleItem, task: TaskEntity): String = withContext(Dispatchers.IO) {
        val timeStr = task.scheduledStart?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
        } ?: "próximamente"

        val roleFallback = when (role.id) {
            "STRICT_COACH" -> "🏋️‍♂️ [Entrenador] ¡A trabajar! Tarea agendada: '${task.title}' para las $timeStr. ¡Sin excusas!"
            "FORMAL_SECRETARY" -> "💼 [Secretario] Le confirmo que la tarea '${task.title}' ha sido programada a las $timeStr."
            "CASUAL_FRIEND" -> "✌️ [Amigo] ¡Listo bro! Nos vemos a las $timeStr con '${task.title}'."
            else -> "[${role.displayName}] ${role.customPhrase} '${task.title}' a las $timeStr"
        }

        val model = generativeModel ?: return@withContext roleFallback

        val prompt = """
            Eres un asistente personal con el rol exclusivo de '${role.displayName}'.
            Personalidad: ${role.description}.
            Tarea creada: "${task.title}", programada a las $timeStr (${task.durationMinutes} min).
            Genera una sola frase directa y muy expresiva en primera persona adaptada estrictamente a tu personalidad. No incluyas comillas ni explicaciones adicionales.
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            val generatedText = response.text?.trim()
            if (!generatedText.isNullOrEmpty()) {
                Log.d("GeminiRoleService", "Respuesta IA Gemini recibida con éxito: $generatedText")
                generatedText
            } else {
                roleFallback
            }
        } catch (e: Exception) {
            Log.e("GeminiRoleService", "Error al consultar la API de Gemini: ${e.message}", e)
            roleFallback
        }
    }

    /**
     * Genera un mensaje de pre-alerta notificando la antelación restante.
     */
    suspend fun generateAdvanceNotification(
        role: RoleItem,
        taskTitle: String,
        scheduledStartMs: Long,
        leadMinutes: Int,
        durationMinutes: Int
    ): String = withContext(Dispatchers.IO) {
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(scheduledStartMs))

        val fallback = when (role.id) {
            "STRICT_COACH" -> "🏋️‍♂️ [Entrenador] Faltan $leadMinutes min para '$taskTitle'. ¡Prepárate ya!"
            "FORMAL_SECRETARY" -> "💼 [Secretario] Le recordamos que su compromiso '$taskTitle' inicia a las $timeStr (en $leadMinutes min)."
            "CASUAL_FRIEND" -> "✌️ [Amigo] Ey, en $leadMinutes min toca '$taskTitle'. ¡No te despistes!"
            else -> "${role.displayName}: Recuerda '$taskTitle' a las $timeStr"
        }

        val model = generativeModel ?: return@withContext fallback

        val prompt = """
            Eres el asistente '${role.displayName}' con personalidad: ${role.description}.
            Aviso de pre-alerta: Quedan $leadMinutes minutos para empezar "$taskTitle" (a las $timeStr, duración: $durationMinutes min).
            Genera una sola frase urgente, corta e impactante según tu personalidad. No uses comillas.
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            val text = response.text?.trim()
            if (!text.isNullOrEmpty()) {
                Log.d("GeminiRoleService", "Pre-alerta IA Gemini recibida con éxito: $text")
                text
            } else {
                fallback
            }
        } catch (e: Exception) {
            Log.e("GeminiRoleService", "Error al consultar pre-alerta en la API de Gemini: ${e.message}", e)
            fallback
        }
    }

    /**
     * Usa la IA para ordenar una lista de tareas de manera lógica y productiva.
     * Devuelve la lista de IDs en el orden recomendado.
     */
    suspend fun rankTasksPriority(tasks: List<TaskEntity>): List<Long> = withContext(Dispatchers.IO) {
        if (tasks.isEmpty()) return@withContext emptyList()
        val model = generativeModel ?: return@withContext tasks.map { it.id }

        val taskListStr = tasks.joinToString("\n") { "- ID:${it.id}: ${it.title} (${it.durationMinutes} min)" }

        val prompt = """
            Como experto en productividad personal, analiza esta lista de tareas pendientes:
            $taskListStr
            
            Ordénalas de la forma más eficiente para un ser humano (considerando ritmos circadianos, importancia lógica y agrupamiento por temas).
            Responde ÚNICAMENTE con la lista de IDs separados por comas, sin espacios ni explicaciones.
            Ejemplo de respuesta: 1,4,2,3
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            val rawText = response.text?.trim() ?: ""
            val cleanText = rawText
                .replace("```json", "")
                .replace("```", "")
                .replace("[", "")
                .replace("]", "")
                .trim()
            val ids = cleanText.split(",", "\n", " ").mapNotNull { it.trim().toLongOrNull() }.distinct()
            if (ids.isNotEmpty() && ids.size == tasks.size && ids.containsAll(tasks.map { it.id })) {
                Log.d("GeminiRoleService", "Ranking IA exitoso: $ids")
                ids
            } else {
                Log.w("GeminiRoleService", "Respuesta de ranking no coincide con IDs esperados: '$rawText'")
                tasks.map { it.id }
            }
        } catch (e: Exception) {
            Log.e("GeminiRoleService", "Error en ranking IA: ${e.message}")
            tasks.map { it.id }
        }
    }
}
