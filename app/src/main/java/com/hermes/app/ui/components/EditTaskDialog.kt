package com.hermes.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var description by remember { mutableStateOf(task.description ?: "") }
    var priority by remember { mutableIntStateOf(task.priority) }
    var duration by remember { mutableIntStateOf(task.durationMinutes) }
    var reminderLeadMinutes by remember { mutableIntStateOf(task.reminderLeadMinutes) }
    
    var isFixed by remember { mutableStateOf(task.isFixed) }
    var hasEndTime by remember { mutableStateOf(task.scheduledEnd != null) }

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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Título") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Notas / Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 1,
                            maxLines = 3
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Hora Fija", color = if (isFixed) NeonAmber else TextSecondary)
                        Switch(checked = isFixed, onCheckedChange = { isFixed = it })
                    }
                }

                if (isFixed) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Incluir hora de fin", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Checkbox(checked = hasEndTime, onCheckedChange = { hasEndTime = it })
                        }
                    }

                    item {
                        var showStart by remember { mutableStateOf(false) }
                        var showEnd by remember { mutableStateOf(false) }
                        val startState = rememberTimePickerState(hour, minute, true)
                        val endState = rememberTimePickerState(endHour, endMinute, true)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Inicio", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                OutlinedButton(onClick = { showStart = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
                                }
                            }
                            if (hasEndTime) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Fin", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    OutlinedButton(onClick = { showEnd = true }, modifier = Modifier.fillMaxWidth()) {
                                        Text("${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}")
                                    }
                                }
                            }
                        }

                        if (showStart) {
                            TimePickerDialog(onDismissRequest = { showStart = false }, confirmButton = {
                                TextButton(onClick = { hour = startState.hour; minute = startState.minute; if (hasEndTime) updateDurationFromHours(); showStart = false }) { Text("OK") }
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
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Duración estimada", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("${duration}m", style = MaterialTheme.typography.labelMedium, color = NeonMagenta, fontWeight = FontWeight.Bold)
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf(
                                15 to "15m", 30 to "30m", 45 to "45m", 60 to "1h", 90 to "1.5h", 
                                120 to "2h", 180 to "3h", 240 to "4h", 480 to "8h", 720 to "12h", 1440 to "24h"
                            )) { (mins, label) ->
                                val selected = duration == mins
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (selected) NeonMagenta else SurfaceVariantDark,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            duration = mins
                                            if (isFixed && hasEndTime) updateDurationFromHours()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.Black else TextPrimary,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = duration.toString(),
                            onValueChange = { input ->
                                val valMins = input.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                duration = valMins
                            },
                            label = { Text("Duración en minutos (ej. 30, 90, 120)", color = TextSecondary, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonMagenta)
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Notificación con antelación", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf(
                                0 to "En el momento (0m)", 5 to "5m", 15 to "15m", 30 to "30m",
                                60 to "1h", 120 to "2h", 1440 to "1 día"
                            )) { (mins, label) ->
                                val selected = reminderLeadMinutes == mins
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (selected) NeonCyan else SurfaceVariantDark,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { reminderLeadMinutes = mins }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.Black else TextPrimary,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = reminderLeadMinutes.toString(),
                            onValueChange = { input ->
                                val m = input.toIntOrNull()?.coerceAtLeast(0) ?: 0
                                reminderLeadMinutes = m
                            },
                            label = { Text("Antelación personalizada (min)", color = TextSecondary, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
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
                val finalEnd = if (isFixed && hasEndTime) {
                    val endCal = (cal.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, endHour)
                        set(Calendar.MINUTE, endMinute)
                    }
                    if (endCal.before(cal)) endCal.add(Calendar.DAY_OF_YEAR, 1)
                    endCal.timeInMillis
                } else if (isFixed) {
                    null
                } else {
                    null
                }

                onSave(task.copy(
                    title = title,
                    description = description.ifBlank { null },
                    durationMinutes = duration,
                    reminderLeadMinutes = reminderLeadMinutes,
                    priority = priority,
                    isFixed = isFixed,
                    scheduledStart = if (isFixed) cal.timeInMillis else task.scheduledStart,
                    scheduledEnd = finalEnd
                ))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
