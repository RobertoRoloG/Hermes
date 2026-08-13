package com.hermes.app.domain.ai

import android.util.Log
import com.google.gson.Gson
import com.hermes.app.BuildConfig
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.domain.RoleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class GroqRoleService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val apiKey: String = BuildConfig.GROQ_API_KEY
        .replace("\"", "")
        .replace("'", "")
        .trim()

    // Modelo activo recomendado de Groq
    private val MODEL_NAME = "llama-3.3-70b-versatile"

    private suspend fun callGroqApi(prompt: String, systemPrompt: String? = null): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            Log.e("GroqRoleService", "API Key de Groq no encontrada en BuildConfig.")
            return@withContext null
        }

        val messages = mutableListOf<Map<String, String>>()
        if (systemPrompt != null) {
            messages.add(mapOf("role" to "system", "content" to systemPrompt))
        }
        messages.add(mapOf("role" to "user", "content" to prompt))

        val requestBody = mapOf(
            "model" to MODEL_NAME,
            "messages" to messages,
            "temperature" to 0.7,
            "max_tokens" to 500
        )

        val jsonBody = gson.toJson(requestBody)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GroqRoleService", "Error en API Groq: ${response.code} ${response.message}")
                    return@withContext null
                }

                val responseData = response.body?.string() ?: return@withContext null
                val jsonResponse = gson.fromJson(responseData, GroqResponse::class.java)
                val content = jsonResponse.choices.firstOrNull()?.message?.content?.trim()
                
                if (!content.isNullOrEmpty()) {
                    Log.d("GroqRoleService", "Respuesta Groq recibida con éxito.")
                    return@withContext content
                }
            }
        } catch (e: Exception) {
            Log.e("GroqRoleService", "Fallo al conectar con Groq: ${e.message}")
        }
        null
    }

    suspend fun generateRoleNotification(role: RoleItem, task: TaskEntity): String = withContext(Dispatchers.IO) {
        val timeStr = task.scheduledStart?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
        } ?: "próximamente"

        val roleFallback = "[FALLBACK] ${role.displayName}: ${role.customPhrase} '${task.title}' a las $timeStr"

        val systemPrompt = "Eres un asistente personal con el rol exclusivo de '${role.displayName}'. Personalidad: ${role.description}."
        val prompt = """
            Tarea creada: "${task.title}", programada a las $timeStr.
            Genera una sola frase corta, directa y muy expresiva en primera persona adaptada estrictamente a tu personalidad. No incluyas comillas ni explicaciones adicionales.
        """.trimIndent()

        callGroqApi(prompt, systemPrompt) ?: roleFallback
    }

    suspend fun generateAdvanceNotification(
        role: RoleItem,
        taskTitle: String,
        scheduledStartMs: Long,
        leadMinutes: Int,
        durationMinutes: Int?
    ): String? = withContext(Dispatchers.IO) {
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(scheduledStartMs))
        val leadStr = if (leadMinutes > 0) "Quedan $leadMinutes minutos para empezar" else "Empieza ahora mismo"
        val durText = durationMinutes?.let { "duración: $it min" } ?: "sin duración definida"

        val systemPrompt = "Eres el asistente '${role.displayName}' con personalidad: ${role.description}."
        val prompt = """
            Aviso de notificación: $leadStr "$taskTitle" (a las $timeStr, $durText).
            Genera una sola frase urgente, corta e impactante según tu personalidad. No uses comillas.
        """.trimIndent()

        callGroqApi(prompt, systemPrompt)
    }

    suspend fun rankTasksPriority(tasks: List<TaskEntity>): List<Long> = withContext(Dispatchers.IO) {
        if (tasks.isEmpty()) return@withContext emptyList()

        val taskListStr = tasks.joinToString("\n") { "- ID:${it.id}: ${it.title} (${it.durationMinutes?.let { d -> "$d min" } ?: "sin duración"})" }

        val systemPrompt = "Eres un experto en productividad personal."
        val prompt = """
            Analiza esta lista de tareas pendientes:
            $taskListStr
            
            Ordénalas de la forma más eficiente para un ser humano (considerando ritmos circadianos e importancia lógica).
            Responde ÚNICAMENTE con la lista de IDs separados por comas.
            Ejemplo de respuesta: 1,4,2,3
        """.trimIndent()

        try {
            val rawText = callGroqApi(prompt, systemPrompt) ?: ""
            val ids = rawText.split(",", "\n", " ").mapNotNull { it.trim().toLongOrNull() }.distinct()
            if (ids.isNotEmpty() && ids.size == tasks.size) {
                ids
            } else {
                tasks.map { it.id }
            }
        } catch (e: Exception) {
            tasks.map { it.id }
        }
    }

    // Data classes para parsear respuesta de Groq (formato OpenAI)
    private data class GroqResponse(val choices: List<Choice>)
    private data class Choice(val message: Message)
    private data class Message(val content: String)
}
