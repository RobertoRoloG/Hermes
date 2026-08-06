package com.hermes.app.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.receiver.TaskNotificationReceiver

object NotificationScheduler {

    const val CHANNEL_ID = "hermes_reminders_channel"
    private const val CHANNEL_NAME = "Recordatorios Hermes"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones y pre-alertas de tareas de Hermes"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleTaskNotification(context: Context, task: TaskEntity) {
        val startMs = task.scheduledStart ?: return
        val now = System.currentTimeMillis()

        val endMs = task.scheduledEnd ?: (startMs + task.durationMinutes * 60 * 1000L)
        if (endMs <= now) return

        val leadMs = task.reminderLeadMinutes * 60 * 1000L
        var triggerTimeMs = startMs - leadMs

        if (triggerTimeMs <= now) {
            triggerTimeMs = now + 1000L
        }

        // Programar preparación de la IA 2 minutos antes del trigger real
        val prepTriggerTimeMs = triggerTimeMs - (2 * 60 * 1000L)
        if (prepTriggerTimeMs > now) {
            scheduleAlarm(context, task, prepTriggerTimeMs, isPrepareOnly = true)
        } else {
            // Disparar pre-generación de IA INMEDIATAMENTE al guardar la tarea
            val prepIntent = Intent(context, TaskNotificationReceiver::class.java).apply {
                action = TaskNotificationReceiver.ACTION_PREPARE_NOTIFICATION
                putExtra("task_id", task.id)
                putExtra("task_title", task.title)
                putExtra("scheduled_start", task.scheduledStart)
                putExtra("reminder_lead_minutes", task.reminderLeadMinutes)
                putExtra("task_duration", task.durationMinutes)
            }
            context.sendBroadcast(prepIntent)
        }

        // Programar notificación real
        scheduleAlarm(context, task, triggerTimeMs, isPrepareOnly = false)
    }

    private fun scheduleAlarm(context: Context, task: TaskEntity, triggerTimeMs: Long, isPrepareOnly: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            if (isPrepareOnly) {
                action = TaskNotificationReceiver.ACTION_PREPARE_NOTIFICATION
            }
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("scheduled_start", task.scheduledStart)
            putExtra("reminder_lead_minutes", task.reminderLeadMinutes)
            putExtra("task_duration", task.durationMinutes)
        }

        // Request code diferenciado para preparación y disparo
        val requestCode = getAlarmRequestCode(task.id, isPrepareOnly)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMs, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            }
        } catch (e: Exception) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    fun cancelTaskNotification(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancelar ambas alarmas
        listOf(getAlarmRequestCode(taskId, false), getAlarmRequestCode(taskId, true)).forEach { requestCode ->
            val intent = Intent(context, TaskNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    fun getAlarmRequestCode(taskId: Long, isPrepareOnly: Boolean): Int {
        val baseHash = (taskId xor (taskId ushr 32)).toInt() and 0x3FFFFFFF
        return if (isPrepareOnly) (baseHash * 2 + 1) else (baseHash * 2)
    }
}
