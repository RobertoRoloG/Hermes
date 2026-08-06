package com.hermes.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hermes.app.data.local.HermesDatabase
import com.hermes.app.utils.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.LOCKED_BOOT_COMPLETED") {
            Log.d("BootReceiver", "Reinicio detectado ($action). Re-programando alarmas activas...")
            
            NotificationScheduler.createNotificationChannel(context)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = HermesDatabase.getDatabase(context)
                    val scheduledTasks = db.taskDao().getActiveScheduledTasks()
                    
                    var rescheduledCount = 0
                    val now = System.currentTimeMillis()
                    for (task in scheduledTasks) {
                        val endMs = task.scheduledEnd ?: ((task.scheduledStart ?: 0L) + task.durationMinutes * 60 * 1000L)
                        if (endMs > now) {
                            NotificationScheduler.scheduleTaskNotification(context, task)
                            rescheduledCount++
                        }
                    }
                    Log.d("BootReceiver", "Se re-programaron $rescheduledCount notificaciones activas exitosamente.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error re-programando notificaciones tras reinicio: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
