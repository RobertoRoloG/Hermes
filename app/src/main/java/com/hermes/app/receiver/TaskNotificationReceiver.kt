package com.hermes.app.receiver

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hermes.app.data.local.HermesDatabase
import com.hermes.app.domain.RoleManager
import com.hermes.app.domain.ai.GeminiRoleService
import com.hermes.app.utils.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COMPLETE_TASK = "com.hermes.app.ACTION_COMPLETE_TASK"
        const val ACTION_SNOOZE_TASK = "com.hermes.app.ACTION_SNOOZE_TASK"
        const val ACTION_PREPARE_NOTIFICATION = "com.hermes.app.ACTION_PREPARE_NOTIFICATION"
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("task_id", -1L)
        val taskTitle = intent.getStringExtra("task_title") ?: "Tarea"
        val scheduledStart = intent.getLongExtra("scheduled_start", System.currentTimeMillis())
        val leadMinutes = intent.getIntExtra("reminder_lead_minutes", 15)
        val duration = intent.getIntExtra("task_duration", 30)

        val action = intent.action

        if (action == ACTION_PREPARE_NOTIFICATION) {
            if (taskId != -1L) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = HermesDatabase.getDatabase(context)
                        val roleManager = RoleManager(context)
                        val geminiService = GeminiRoleService()
                        
                        val task = db.taskDao().getTaskById(taskId)
                        if (task != null && !task.isCompleted) {
                            val activeRole = if (task.createdRole.isNotBlank()) {
                                roleManager.getRoleByDisplayName(task.createdRole)
                            } else {
                                roleManager.getActiveRole()
                            }

                            val message = geminiService.generateAdvanceNotification(
                                role = activeRole,
                                taskTitle = task.title,
                                scheduledStartMs = task.scheduledStart ?: System.currentTimeMillis(),
                                leadMinutes = task.reminderLeadMinutes,
                                durationMinutes = task.durationMinutes,
                            )
                            db.taskDao().updatePreGeneratedMessage(taskId, message)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            return
        }

        if (action == ACTION_COMPLETE_TASK) {
            if (taskId != -1L) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = HermesDatabase.getDatabase(context)
                        db.taskDao().updateTaskStatus(taskId, isCompleted = true)
                        val manager = NotificationManagerCompat.from(context)
                        manager.cancel(taskId.toInt())

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "✓ Tarea '$taskTitle' completada", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            return
        }

        if (action == ACTION_SNOOZE_TASK) {
            if (taskId != -1L) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = HermesDatabase.getDatabase(context)
                        val task = db.taskDao().getTaskById(taskId)
                        if (task != null && task.scheduledStart != null) {
                            val snoozeMs = 15 * 60 * 1000L
                            val newStart = task.scheduledStart + snoozeMs
                            val newEnd = (task.scheduledEnd ?: task.scheduledStart) + snoozeMs
                            val updated = task.copy(scheduledStart = newStart, scheduledEnd = newEnd)

                            db.taskDao().updateTaskSchedule(taskId, newStart, newEnd, task.isAutoScheduled)
                            NotificationScheduler.scheduleTaskNotification(context, updated)

                            val manager = NotificationManagerCompat.from(context)
                            manager.cancel(taskId.toInt())

                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "⏱ '$taskTitle' postergada 15 minutos", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            return
        }

        // LÓGICA HABITUAL DE DISPARO DE PRE-ALERTA / NOTIFICACIÓN
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val db = HermesDatabase.getDatabase(context)
                val task = if (taskId != -1L) db.taskDao().getTaskById(taskId) else null
                
                if (task != null && task.isCompleted) {
                    return@launch
                }

                val roleManager = RoleManager(context)
                val activeRole = if (task != null && task.createdRole.isNotBlank()) {
                    roleManager.getRoleByDisplayName(task.createdRole)
                } else {
                    roleManager.getActiveRole()
                }

                // Usar el mensaje pre-generado por la IA o generarlo ahora mismo
                var message = task?.preGeneratedMessage

                if (message == null) {
                    try {
                        val geminiService = GeminiRoleService()
                        message = geminiService.generateAdvanceNotification(
                            role = activeRole,
                            taskTitle = taskTitle,
                            scheduledStartMs = scheduledStart,
                            leadMinutes = leadMinutes,
                            durationMinutes = duration,
                        )
                        if (taskId != -1L) {
                            db.taskDao().updatePreGeneratedMessage(taskId, message)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        message = "${activeRole.displayName}: Recuerda '$taskTitle' (${duration} min)."
                    }
                }

                val titleText = if (leadMinutes > 0) "$taskTitle (en $leadMinutes min)" else "$taskTitle (Empieza ahora)"

                // Acción Completar
                val completeIntent = Intent(context, TaskNotificationReceiver::class.java).apply {
                    this.action = ACTION_COMPLETE_TASK
                    putExtra("task_id", taskId)
                    putExtra("task_title", taskTitle)
                }
                val completePendingIntent = PendingIntent.getBroadcast(
                    context,
                    (taskId * 10 + 1).toInt(),
                    completeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Acción Postergar 15 min
                val snoozeIntent = Intent(context, TaskNotificationReceiver::class.java).apply {
                    this.action = ACTION_SNOOZE_TASK
                    putExtra("task_id", taskId)
                    putExtra("task_title", taskTitle)
                }
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    (taskId * 10 + 2).toInt(),
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
                    .setSmallIcon(com.hermes.app.R.mipmap.ic_launcher)
                    .setContentTitle(titleText)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .addAction(0, "✓ Completar", completePendingIntent)
                    .addAction(0, "⏱ Postergar 15m", snoozePendingIntent)

                val manager = NotificationManagerCompat.from(context)
                manager.notify(taskId.toInt(), builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
