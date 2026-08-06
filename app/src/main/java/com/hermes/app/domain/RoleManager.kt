package com.hermes.app.domain

import android.content.Context
import android.content.SharedPreferences
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.domain.ai.GeminiRoleService
import org.json.JSONArray
import org.json.JSONObject

data class RoleItem(
    val id: String,
    val displayName: String,
    val description: String,
    val customPhrase: String
)

class RoleManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("hermes_settings", Context.MODE_PRIVATE)

    private val geminiRoleService = GeminiRoleService()

    companion object {
        private const val KEY_ACTIVE_ROLE_IDS = "active_role_ids_set"
        private const val KEY_ASSISTANT_NAME = "assistant_name"
        private const val KEY_CUSTOM_ROLES_JSON = "custom_roles_json"
        private const val KEY_WORK_DAY_START_HOUR = "work_day_start_hour"
        private const val KEY_WORK_DAY_END_HOUR = "work_day_end_hour"

        val DEFAULT_ROLES = listOf(
            RoleItem(
                id = "STRICT_COACH",
                displayName = "Entrenador Estricto",
                description = "Exige máxima disciplina y cumplimiento inmediato.",
                customPhrase = "¡Sin excusas! Tarea registrada:"
            ),
            RoleItem(
                id = "FORMAL_SECRETARY",
                displayName = "Secretario Formal",
                description = "Mantiene un tono profesional y corporativo.",
                customPhrase = "Confirmado: Se ha añadido la tarea:"
            ),
            RoleItem(
                id = "CASUAL_FRIEND",
                displayName = "Amigo Casual",
                description = "Tono relajado y distendido de agenda.",
                customPhrase = "¡Listo colega! Tarea agendada:"
            )
        )
    }

    fun getWorkDayStartHour(): Int = prefs.getInt(KEY_WORK_DAY_START_HOUR, 8)
    fun setWorkDayStartHour(hour: Int) { prefs.edit().putInt(KEY_WORK_DAY_START_HOUR, hour.coerceIn(0, 23)).apply() }

    fun getWorkDayEndHour(): Int = prefs.getInt(KEY_WORK_DAY_END_HOUR, 20)
    fun setWorkDayEndHour(hour: Int) { prefs.edit().putInt(KEY_WORK_DAY_END_HOUR, hour.coerceIn(1, 24)).apply() }

    fun getAllRoles(): List<RoleItem> {
        val customJson = prefs.getString(KEY_CUSTOM_ROLES_JSON, null)
        val list = DEFAULT_ROLES.toMutableList()

        if (!customJson.isNull_or_empty()) {
            try {
                val jsonArray = JSONArray(customJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        RoleItem(
                            id = obj.getString("id"),
                            displayName = obj.getString("displayName"),
                            description = obj.getString("description"),
                            customPhrase = obj.optString("customPhrase", "Tarea agendada:")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return list
    }

    fun addCustomRole(name: String, description: String, phrase: String): RoleItem {
        val id = "CUSTOM_${System.currentTimeMillis()}"
        val newRole = RoleItem(id, name, description, phrase)
        val currentRoles = getAllRoles().filter { it.id.startsWith("CUSTOM_") }.toMutableList()
        currentRoles.add(newRole)

        val jsonArray = JSONArray()
        for (r in currentRoles) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("displayName", r.displayName)
                put("description", r.description)
                put("customPhrase", r.customPhrase)
            }
            jsonArray.put(obj)
        }

        prefs.edit().putString(KEY_CUSTOM_ROLES_JSON, jsonArray.toString()).apply()
        // Activar el nuevo rol en el conjunto seleccionado
        val activeIds = getSelectedRoleIds().toMutableSet()
        activeIds.add(id)
        setSelectedRoleIds(activeIds)
        return newRole
    }

    fun addCustomRole(name: String, description: String, priorityBoost: Int, phrase: String): RoleItem {
        return addCustomRole(name, description, phrase)
    }

    fun deleteRole(roleId: String): Boolean {
        val currentRoles = getAllRoles().filter { it.id.startsWith("CUSTOM_") && it.id != roleId }
        val jsonArray = JSONArray()
        for (r in currentRoles) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("displayName", r.displayName)
                put("description", r.description)
                put("customPhrase", r.customPhrase)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_ROLES_JSON, jsonArray.toString()).apply()

        val activeIds = getSelectedRoleIds().toMutableSet()
        activeIds.remove(roleId)
        if (activeIds.isEmpty()) {
            activeIds.add("STRICT_COACH")
        }
        setSelectedRoleIds(activeIds)
        return true
    }

    fun getSelectedRoleIds(): Set<String> {
        val set = prefs.getStringSet(KEY_ACTIVE_ROLE_IDS, null)
        return set ?: setOf("STRICT_COACH", "FORMAL_SECRETARY", "CASUAL_FRIEND")
    }

    fun setSelectedRoleIds(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_ACTIVE_ROLE_IDS, ids).apply()
    }

    fun toggleRoleSelection(roleId: String) {
        val current = getSelectedRoleIds().toMutableSet()
        if (current.contains(roleId)) {
            if (current.size > 1) {
                current.remove(roleId)
            }
        } else {
            current.add(roleId)
        }
        setSelectedRoleIds(current)
    }

    fun setActiveRole(roleId: String) {
        toggleRoleSelection(roleId)
    }

    fun getSelectedRoles(): List<RoleItem> {
        val ids = getSelectedRoleIds()
        return getAllRoles().filter { it.id in ids }
    }

    fun getActiveRole(): RoleItem {
        return getSelectedRoles().firstOrNull() ?: getAllRoles().first()
    }

    fun getRoleByDisplayName(displayName: String): RoleItem {
        return getAllRoles().find { it.displayName.equals(displayName, ignoreCase = true) }
            ?: getActiveRole()
    }

    fun getAssistantName(): String {
        return prefs.getString(KEY_ASSISTANT_NAME, "Hermes AI") ?: "Hermes AI"
    }

    fun setAssistantName(name: String) {
        prefs.edit().putString(KEY_ASSISTANT_NAME, name).apply()
    }

    /**
     * Asigna un rol ALEATORIO (de los roles seleccionados) a la tarea.
     * La prioridad de la tarea no se modifica en absoluto.
     */
    fun applyRoleContextToTask(task: TaskEntity): TaskEntity {
        val selectedRoles = getSelectedRoles()
        val randomRole = if (selectedRoles.isNotEmpty()) selectedRoles.random() else getActiveRole()
        return task.copy(
            createdRole = randomRole.displayName
        )
    }

    /**
     * Generación síncrona/fallback para mensajes locales.
     */
    fun formatNotificationMessage(taskTitle: String): String {
        val assistantName = getAssistantName()
        val role = getActiveRole()
        return "[$assistantName] ${role.customPhrase} '$taskTitle'"
    }

    /**
     * Generación dinámica por IA con la personalidad del rol asignado a la tarea.
     */
    suspend fun generateDynamicNotificationMessage(task: TaskEntity): String {
        val assignedRole = getRoleByDisplayName(task.createdRole)
        return geminiRoleService.generateRoleNotification(assignedRole, task)
    }

    suspend fun generateAdvanceNotificationForTask(task: TaskEntity): String {
        val assignedRole = getRoleByDisplayName(task.createdRole)
        val startMs = task.scheduledStart ?: System.currentTimeMillis()
        return geminiRoleService.generateAdvanceNotification(
            role = assignedRole,
            taskTitle = task.title,
            scheduledStartMs = startMs,
            leadMinutes = task.reminderLeadMinutes,
            durationMinutes = task.durationMinutes
        )
    }

    /**
     * Usa a Gemini para obtener el orden ideal de ejecución de un grupo de tareas.
     */
    suspend fun getAIRankedTaskIds(tasks: List<TaskEntity>): List<Long> {
        return geminiRoleService.rankTasksPriority(tasks)
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
