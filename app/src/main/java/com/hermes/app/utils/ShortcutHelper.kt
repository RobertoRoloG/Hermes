package com.hermes.app.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.hermes.app.ui.main.MainActivity

object ShortcutHelper {

    private const val SHORTCUT_ID_ADD_TASK = "shortcut_add_task_dynamic"

    /**
     * Actualiza en tiempo real el atajo dinámico del sistema mediante ShortcutManager.
     * Permite cambiar el nombre con el que Google Assistant interactúa con Hermes sin reinstalar.
     */
    fun updateAssistantDynamicShortcut(context: Context, assistantName: String) {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("from_dynamic_shortcut", true)
        }

        val name = assistantName.ifBlank { "Hermes" }

        val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID_ADD_TASK)
            .setShortLabel(name)
            .setLongLabel("Añadir tarea con $name")
            .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_input_add))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    /**
     * Elimina atajos dinámicos si fuera necesario.
     */
    fun removeDynamicShortcuts(context: Context) {
        ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(SHORTCUT_ID_ADD_TASK))
    }
}
