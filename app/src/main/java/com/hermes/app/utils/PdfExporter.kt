package com.hermes.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.ui.stats.StatsTimeRange
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun exportTasksToPdf(
        context: Context,
        tasks: List<TaskEntity>,
        timeRange: StatsTimeRange
    ) {
        try {
            val fileName = if (timeRange == StatsTimeRange.DIARIO) {
                "Hermes_Informe_Diario_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            } else {
                "Hermes_Informe_Semanal_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            }

            val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
            val file = File(exportDir, fileName)

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Formato A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            // 1. CABECERA SUPERIOR
            paint.color = Color.parseColor("#121824") // SurfaceDark
            canvas.drawRect(0f, 0f, 595f, 90f, paint)

            paint.color = Color.parseColor("#00F0FF") // NeonCyan
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("HERMES AI", 30f, 40f, paint)

            paint.color = Color.WHITE
            paint.textSize = 14f
            val headerTitle = if (timeRange == StatsTimeRange.DIARIO) "Informe & Planning Diario" else "Informe & Planning Semanal"
            canvas.drawText(headerTitle, 30f, 65f, paint)

            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            paint.color = Color.parseColor("#8E9AA8")
            paint.textSize = 10f
            canvas.drawText("Generado el: $dateStr", 400f, 65f, paint)

            // 2. RESUMEN DE MÉTRICAS (TARJETA EJECUTIVA)
            var currentY = 110f
            paint.color = Color.parseColor("#1E293B")
            val summaryRect = RectF(30f, currentY, 565f, currentY + 70f)
            canvas.drawRoundRect(summaryRect, 8f, 8f, paint)

            val totalCount = tasks.size
            val completedCount = tasks.count { it.isCompleted }
            val pendingCount = totalCount - completedCount
            val efficiencyPct = if (totalCount > 0) (completedCount.toFloat() / totalCount * 100).toInt() else 0

            paint.color = Color.WHITE
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("RESUMEN DE EFECTIVIDAD", 45f, currentY + 25f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.parseColor("#CBD5E1")
            canvas.drawText("Total Tareas: $totalCount   |   Completadas: $completedCount   |   Pendientes: $pendingCount", 45f, currentY + 48f, paint)

            // Porcentaje grande
            paint.color = Color.parseColor("#00F0FF")
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("$efficiencyPct%", 490f, currentY + 45f, paint)

            currentY += 90f

            // 3. TABLA DE TAREAS
            paint.color = Color.parseColor("#0F172A")
            canvas.drawRect(30f, currentY, 565f, currentY + 25f, paint)

            paint.color = Color.parseColor("#38BDF8")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("HORA", 40f, currentY + 16f, paint)
            canvas.drawText("TÍTULO / TAREA", 110f, currentY + 16f, paint)
            canvas.drawText("DURACIÓN", 330f, currentY + 16f, paint)
            canvas.drawText("PRIORIDAD", 410f, currentY + 16f, paint)
            canvas.drawText("ESTADO", 490f, currentY + 16f, paint)

            currentY += 25f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9f

            for ((index, task) in tasks.take(25).withIndex()) {
                val isEven = index % 2 == 0
                paint.color = if (isEven) Color.parseColor("#F8FAFC") else Color.WHITE
                canvas.drawRect(30f, currentY, 565f, currentY + 22f, paint)

                paint.color = Color.parseColor("#334155")
                val startStr = task.scheduledStart?.let { timeFormat.format(Date(it)) } ?: "--:--"
                canvas.drawText(startStr, 40f, currentY + 15f, paint)

                val displayTitle = if (task.title.length > 35) task.title.take(32) + "..." else task.title
                canvas.drawText(displayTitle, 110f, currentY + 15f, paint)

                canvas.drawText("${task.durationMinutes} min", 330f, currentY + 15f, paint)

                val priorityText = when (task.priority) {
                    3 -> "Alta"
                    2 -> "Media"
                    else -> "Baja"
                }
                paint.color = when (task.priority) {
                    3 -> Color.parseColor("#EF4444")
                    2 -> Color.parseColor("#F59E0B")
                    else -> Color.parseColor("#10B981")
                }
                canvas.drawText(priorityText, 410f, currentY + 15f, paint)

                val statusText = if (task.isCompleted) "✓ Completada" else "Pendiente"
                paint.color = if (task.isCompleted) Color.parseColor("#10B981") else Color.parseColor("#64748B")
                canvas.drawText(statusText, 490f, currentY + 15f, paint)

                currentY += 22f
            }

            // 4. PIE DE PÁGINA
            paint.color = Color.parseColor("#94A3B8")
            paint.textSize = 8f
            canvas.drawText("Documento generado automáticamente por Hermes AI Assistant. Todos los derechos reservados.", 130f, 820f, paint)

            pdfDocument.finishPage(page)

            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()

            // Guardar copia local en Descargas
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
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "Informe Hermes PDF (${if (timeRange == StatsTimeRange.DIARIO) "Diario" else "Semanal"})")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Exportar Informe PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
