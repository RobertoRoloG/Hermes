package com.hermes.app.utils

import android.content.Intent
import com.hermes.app.data.local.entity.TaskEntity

object IntentParser {

    /**
     * Parsea los extras de un Intent entrante enviado por Google Assistant (vía shortcuts.xml o DeepLink)
     * y los convierte en una TaskEntity estructurada.
     */
    fun parseTaskFromIntent(intent: Intent): TaskEntity? {
        val title = intent.getStringExtra("task_title")
            ?: intent.getStringExtra("taste.name")
            ?: intent.data?.getQueryParameter("title")
            ?: return null

        if (title.isBlank()) return null

        val isFixed = intent.getBooleanExtra("is_fixed", false)
        val durationMinutes = intent.getIntExtra("duration", 30)
        val deadlineMs = if (intent.hasExtra("deadline")) intent.getLongExtra("deadline", 0L) else null
        val priority = intent.getIntExtra("priority", 1)

        return TaskEntity(
            title = title.trim(),
            isFixed = isFixed,
            durationMinutes = durationMinutes,
            deadline = if ((deadlineMs != null) && (deadlineMs > 0)) deadlineMs else null,
            priority = priority,
        )
    }
}
