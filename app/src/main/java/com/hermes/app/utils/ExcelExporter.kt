package com.hermes.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.ui.stats.StatsTimeRange
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    /**
     * Genera y exporta el planning (Diario o Semanal) en un archivo CSV compatible con Excel (UTF-8 con BOM y separador ';' habitual en español).
     */
    fun exportTasksToExcel(
        context: Context,
        tasks: List<TaskEntity>,
        timeRange: StatsTimeRange
    ) {
        if (timeRange != StatsTimeRange.DIARIO && timeRange != StatsTimeRange.SEMANAL) {
            Toast.makeText(context, "Exportación disponible solo para Diario y Semanal", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val fileName = if (timeRange == StatsTimeRange.DIARIO) {
                "Hermes_Planning_Diario_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
            } else {
                "Hermes_Planning_Semanal_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
            }

            val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
            val file = File(exportDir, fileName)

            FileOutputStream(file).use { fos ->
                // Escribir UTF-8 BOM para que Excel detecte la codificación de caracteres en español (tildes, ñ)
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                
                OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                    val rangeTitle = if (timeRange == StatsTimeRange.DIARIO) "PLANNING DIARIO DE HERMES AI" else "PLANNING SEMANAL DE HERMES AI"
                    val generatedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                    writer.append("$rangeTitle;Generado el: $generatedDate\n\n")

                    // Cabeceras de columna
                    writer.append("ID;Fecha;Hora Inicio;Hora Fin;Título;Descripción;Duración (min);Prioridad;Tipo;Estado;Rol Asistente\n")

                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                    for (task in tasks) {
                        val taskTime = task.scheduledStart ?: task.createdAt
                        val dateStr = dateFormat.format(Date(taskTime))
                        val startStr = task.scheduledStart?.let { timeFormat.format(Date(it)) } ?: "--:--"
                        val endStr = task.scheduledEnd?.let { timeFormat.format(Date(it)) } ?: "--:--"
                        
                        val priorityStr = when (task.priority) {
                            3 -> "Alta (P3)"
                            2 -> "Media (P2)"
                            else -> "Baja (P1)"
                        }

                        val typeStr = when {
                            task.isFixed -> "Fija"
                            task.isAutoScheduled -> "Auto CSP"
                            else -> "Manual"
                        }

                        val statusStr = if (task.isCompleted) "Completada" else "Pendiente"
                        val safeTitle = task.title.replace(";", ",").replace("\n", " ")
                        val safeDesc = (task.description ?: "").replace(";", ",").replace("\n", " ")

                        writer.append("${task.id};$dateStr;$startStr;$endStr;$safeTitle;$safeDesc;${task.durationMinutes};$priorityStr;$typeStr;$statusStr;${task.createdRole}\n")
                    }

                    // Métricas de resumen al final del archivo
                    val totalCount = tasks.size
                    val completedCount = tasks.count { it.isCompleted }
                    val pendingCount = totalCount - completedCount
                    val efficiencyPct = if (totalCount > 0) (completedCount.toFloat() / totalCount * 100).toInt() else 0

                    writer.append("\n--- RESUMEN DE RENDIMIENTO ---\n")
                    writer.append("Total de Tareas;$totalCount\n")
                    writer.append("Completadas;$completedCount\n")
                    writer.append("Pendientes;$pendingCount\n")
                    writer.append("Porcentaje de Efectividad;$efficiencyPct%\n")
                }
            }

            // Guardar también una copia local permanente en la carpeta de descargas de la app
            val externalDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (externalDir != null) {
                file.copyTo(File(externalDir, fileName), overwrite = true)
            }

            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Planning Hermes (${if (timeRange == StatsTimeRange.DIARIO) "Diario" else "Semanal"})")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Exportar planning a Excel")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
