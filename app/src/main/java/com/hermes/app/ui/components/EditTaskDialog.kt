package com.hermes.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.ui.theme.*
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var priority by remember { mutableIntStateOf(task.priority) }
    var duration by remember { mutableIntStateOf(task.durationMinutes) }
    
    var isFixed by remember { mutableStateOf(task.isFixed) }

    val initialCal = remember(task.scheduledStart) {
        Calendar.getInstance().apply {
            if (task.scheduledStart != null) {
                timeInMillis = task.scheduledStart
            }
        }
    }
    var hour by remember { mutableIntStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(initialCal.get(Calendar.MINUTE)) }
    
    var endHour by remember { mutableIntStateOf(0) }
    var endMinute by remember { mutableIntStateOf(0) }
    
    // Inicializar hora de fin
    LaunchedEffect(hour, minute, duration) {
        val totalStart = hour * 60 + minute
        val totalEnd = totalStart + duration
        endHour = (totalEnd / 60) % 24
        endMinute = totalEnd % 60
    }

    fun updateDurationFromHours() {
        val startTotal = hour * 60 + minute
        var endTotal = endHour * 60 + endMinute
        if (endTotal <= startTotal) endTotal += 24 * 60
        duration = endTotal - startTotal
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Editar Tarea", color = NeonCyan, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Hora Fija", color = if (isFixed) NeonAmber else TextSecondary)
                        Switch(checked = isFixed, onCheckedChange = { isFixed = it })
                    }
                }

                if (isFixed) {
                    item {
                        var showStart by remember { mutableStateOf(false) }
                        var showEnd by remember { mutableStateOf(false) }
                        val startState = rememberTimePickerState(hour, minute, true)
                        val endState = rememberTimePickerState(endHour, endMinute, true)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showStart = true }, modifier = Modifier.weight(1f)) {
                                Text("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
                            }
                            OutlinedButton(onClick = { showEnd = true }, modifier = Modifier.weight(1f)) {
                                Text("${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}")
                            }
                        }

                        if (showStart) {
                            TimePickerDialog(onDismissRequest = { showStart = false }, confirmButton = {
                                TextButton(onClick = { hour = startState.hour; minute = startState.minute; updateDurationFromHours(); showStart = false }) { Text("OK") }
                            }) { TimePicker(startState) }
                        }
                        if (showEnd) {
                            TimePickerDialog(onDismissRequest = { showEnd = false }, confirmButton = {
                                TextButton(onClick = { endHour = endState.hour; endMinute = endState.minute; updateDurationFromHours(); showEnd = false }) { Text("OK") }
                            }) { TimePicker(endState) }
                        }
                    }
                }

                item {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Duración", style = MaterialTheme.typography.labelSmall)
                            Text("${duration}m", color = NeonMagenta)
                        }
                        Slider(
                            value = duration.toFloat(),
                            onValueChange = { duration = ((it / 5f).roundToInt() * 5).coerceAtLeast(5) },
                            valueRange = 5f..480f
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..3).forEach { p ->
                            FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text("P$p") })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val cal = (initialCal.clone() as Calendar).apply {
                    if (isFixed) {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    }
                }
                onSave(task.copy(
                    title = title,
                    priority = priority,
                    durationMinutes = duration,
                    isFixed = isFixed,
                    scheduledStart = if (isFixed) cal.timeInMillis else null,
                    scheduledEnd = if (isFixed) cal.timeInMillis + (duration * 60 * 1000L) else null
                ))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
