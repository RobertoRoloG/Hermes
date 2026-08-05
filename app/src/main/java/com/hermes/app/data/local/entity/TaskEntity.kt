package com.hermes.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val isFixed: Boolean = false,
    val durationMinutes: Int = 30,
    val reminderLeadMinutes: Int = 15, // Minutos de antelación para la pre-alerta
    val deadline: Long? = null,
    val scheduledStart: Long? = null,
    val scheduledEnd: Long? = null,
    val isAutoScheduled: Boolean = false,
    val priority: Int = 1, // 1: Baja, 2: Media, 3: Alta
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val createdRole: String = "DEFAULT",
    val preGeneratedMessage: String? = null // Almacena el mensaje de la IA generado con antelación
)
