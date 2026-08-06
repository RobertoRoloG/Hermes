package com.hermes.app.ui.day

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.ui.components.EditTaskDialog
import com.hermes.app.ui.components.InteractiveTimelineCard
import com.hermes.app.ui.components.LegendBadge
import com.hermes.app.ui.components.TimePickerDialog
import com.hermes.app.ui.theme.*
import com.hermes.app.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDayScreen(
    viewModel: TaskViewModel
) {
    val selectedCalendar by viewModel.selectedCalendar.collectAsState()
    val dayTasks by viewModel.dayTasks.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showCreateModal by remember { mutableStateOf(false) }
    var showHelpBanner by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")) }
    
    val dateDisplay = remember(selectedCalendar.timeInMillis) {
        val today = Calendar.getInstance()
        if (selectedCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            selectedCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        ) {
            "Hoy (${dateFormatter.format(selectedCalendar.time)})"
        } else {
            dateFormatter.format(selectedCalendar.time).replaceFirstChar { it.uppercase() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = NeonCyan
                ),
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showDatePicker = true }
                    ) {
                        Text("Mi Día", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Elegir Fecha", tint = NeonCyan, modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.runAutoScheduler() }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Re-Planificar CSP", tint = NeonCyan)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateModal = true },
                containerColor = NeonCyan,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Tarea")
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Fecha de hoy en pequeño
            val todayDateStr = remember { 
                SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES")).format(Date())
            }
            Text(
                text = todayDateStr.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 1. Carrusel de Fechas
            DateCarousel(selectedCalendar, onDateSelected = { viewModel.setSelectedDate(it) })

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Leyenda y Ayuda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendBadge(label = "FIJA", color = NeonRed)
                    LegendBadge(label = "AUTO", color = NeonCyan)
                    LegendBadge(label = "MANUAL", color = NeonMagenta)
                }
                IconButton(onClick = { showHelpBanner = !showHelpBanner }) {
                    Icon(Icons.Default.Info, contentDescription = "Ayuda", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }

            AnimatedVisibility(visible = showHelpBanner) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("• Tareas Fijas: Horarios bloqueados.", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Text("• Tareas Flexibles: El CSP busca huecos entre 08:00 y 20:00.", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Lista de Tareas (Timeline Style)
            val scheduledTasks = remember(dayTasks) {
                dayTasks.filter { it.scheduledStart != null && (it.isFixed || it.scheduledEnd != null) }
                    .sortedBy { it.scheduledStart }
            }
            
            val unscheduledTasks = remember(dayTasks) {
                dayTasks.filter { it.scheduledStart == null || (!it.isFixed && it.scheduledEnd == null) }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (scheduledTasks.isEmpty() && unscheduledTasks.isEmpty()) {
                    item {
                        EmptyDayPlaceholder()
                    }
                } else {
                    if (unscheduledTasks.isNotEmpty()) {
                        item {
                            Text("Pendientes de Planificar", style = MaterialTheme.typography.labelMedium, color = NeonAmber)
                        }
                        items(unscheduledTasks) { task ->
                            UnscheduledTaskCard(task, onDelete = { taskToDelete = task }, onPlanClick = { viewModel.runAutoScheduler() })
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    if (scheduledTasks.isNotEmpty()) {
                        item {
                            Text("Agenda del Día", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                        }
                        items(scheduledTasks, key = { it.id }) { task ->
                            InteractiveTimelineCard(
                                task = task,
                                timeFormatter = timeFormatter,
                                onToggleComplete = { isChecked -> viewModel.completeTask(task.id, isChecked) },
                                onEditClick = { editingTask = task },
                                onDeleteClick = { taskToDelete = task },
                                onLockTaskAsFixed = { 
                                    viewModel.updateTask(task.copy(isFixed = true, isAutoScheduled = false))
                                },
                                onShiftMinutes = { minutes: Int ->
                                    val start = task.scheduledStart
                                    val end = task.scheduledEnd
                                    if (start != null && end != null) {
                                        val shiftMs = minutes * 60 * 1000L
                                        viewModel.updateTask(task.copy(
                                            scheduledStart = start + shiftMs,
                                            scheduledEnd = end + shiftMs,
                                            isAutoScheduled = false
                                        ))
                                    }
                                }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Modales y Diálogos
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedCalendar.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newCal = Calendar.getInstance().apply { timeInMillis = millis }
                        viewModel.setSelectedDate(newCal)
                    }
                    showDatePicker = false
                }) { Text("Seleccionar", color = NeonCyan) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
            colors = DatePickerDefaults.colors(containerColor = SurfaceDark)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCreateModal) {
        CreateTaskDialog(
            initialDate = selectedCalendar,
            onDismiss = { showCreateModal = false },
            onSave = { rawTask, days, weeks ->
                if (days.isNotEmpty()) {
                    val cal = rawTask.scheduledStart?.let { Calendar.getInstance().apply { timeInMillis = it } } ?: selectedCalendar
                    viewModel.addTasksForDaysOfWeek(
                        baseTask = rawTask, 
                        selectedDaysOfWeek = days, 
                        startCalendar = cal, 
                        fixedStartHour = cal.get(Calendar.HOUR_OF_DAY), 
                        fixedStartMinute = cal.get(Calendar.MINUTE),
                        numWeeks = weeks
                    )
                } else {
                    viewModel.addTask(rawTask)
                }
                showCreateModal = false
            }
        )
    }

    editingTask?.let { task ->
        EditTaskDialog(task = task, onDismiss = { editingTask = null }, onSave = { viewModel.updateTask(it); editingTask = null })
    }

    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            containerColor = SurfaceDark,
            title = { Text("¿Eliminar tarea?", color = NeonRed) },
            text = { Text("Se borrará '${task.title}'") },
            confirmButton = {
                Button(onClick = { viewModel.deleteTask(task); taskToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = NeonRed)) {
                    Text("Eliminar")
                }
            },
            dismissButton = { TextButton(onClick = { taskToDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun DateCarousel(selectedCalendar: Calendar, onDateSelected: (Calendar) -> Unit) {
    val today = remember { Calendar.getInstance() }
    val dateList = remember(selectedCalendar.timeInMillis) {
        (-7..7).map { offset ->
            (selectedCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
        }
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        items(dateList) { dayCal ->
            val isSelected = isSameDay(dayCal, selectedCalendar)
            val isToday = isSameDay(dayCal, today)
            val dayName = SimpleDateFormat("EEE", Locale("es", "ES")).format(dayCal.time).uppercase()
            val dayNum = dayCal.get(Calendar.DAY_OF_MONTH).toString()

            Card(
                colors = CardDefaults.cardColors(containerColor = if (isSelected) NeonCyan else SurfaceDark),
                modifier = Modifier.width(60.dp).clickable { onDateSelected(dayCal) }.border(1.dp, if (isSelected) NeonCyan else SurfaceVariantDark, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (isToday) "HOY" else dayName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else TextSecondary)
                    Text(text = dayNum, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else TextPrimary)
                }
            }
        }
    }
}

@Composable
fun UnscheduledTaskCard(task: TaskEntity, onDelete: () -> Unit, onPlanClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth().border(1.dp, NeonAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("${task.durationMinutes} min | Prioridad ${task.priority}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            IconButton(onClick = onPlanClick) { Icon(Icons.Default.PlayArrow, contentDescription = "Planificar", tint = NeonAmber) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = NeonRed) }
        }
    }
}

@Composable
fun EmptyDayPlaceholder() {
    Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(48.dp), tint = SurfaceVariantDark)
        Spacer(modifier = Modifier.height(12.dp))
        Text("No hay tareas para hoy", color = TextSecondary, fontWeight = FontWeight.Bold)
    }
}

private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    initialDate: Calendar,
    onDismiss: () -> Unit,
    onSave: (TaskEntity, Set<Int>, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(30) }
    var priority by remember { mutableIntStateOf(1) }
    // Sección ¿Cuándo?
    var letHermesChooseDate by remember { mutableStateOf(false) }
    var reminderLeadMinutes by remember { mutableIntStateOf(15) }
    var availableFromDate by remember { mutableStateOf(initialDate) }
    var showAvailableFromDatePicker by remember { mutableStateOf(false) }

    var taskDate by remember { mutableStateOf(initialDate) }
    var deadlineDate by remember { mutableStateOf<Calendar?>(null) }
    var isFixed by remember { mutableStateOf(false) }
    var hasEndTime by remember { mutableStateOf(true) }
    var hour by remember { mutableIntStateOf(9) }
    var minute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(9) }
    var endMinute by remember { mutableIntStateOf(30) }

    // Sección Repetición
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var numWeeks by remember { mutableIntStateOf(1) }

    var showDatePickerInDialog by remember { mutableStateOf(false) }
    var showDeadlinePicker by remember { mutableStateOf(false) }

    val daysOfWeekList = listOf(
        Calendar.MONDAY to "L", Calendar.TUESDAY to "M", Calendar.WEDNESDAY to "X",
        Calendar.THURSDAY to "J", Calendar.FRIDAY to "V", Calendar.SATURDAY to "S",
        Calendar.SUNDAY to "D"
    )

    fun updateDurationFromHours() {
        val startTotal = hour * 60 + minute
        var endTotal = endHour * 60 + endMinute
        if (endTotal <= startTotal) endTotal += 24 * 60
        duration = endTotal - startTotal
    }

    fun updateEndHourFromDuration() {
        val startTotal = hour * 60 + minute
        val endTotal = startTotal + duration
        endHour = (endTotal / 60) % 24
        endMinute = endTotal % 60
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Nueva Tarea", color = NeonCyan, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECCIÓN 1: ¿QUÉ VAS A HACER?
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("¿QUÉ VAS A HACER?")
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("Ej: Ir al gimnasio") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Notas / Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 1,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
                        )
                        
                        val durationText = remember(duration) {
                            val h = duration / 60
                            val m = duration % 60
                            when {
                                h > 0 && m > 0 -> "${h}h ${m}m ($duration min)"
                                h > 0 -> "${h}h ($duration min)"
                                else -> "${m}m"
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Duración estimada", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(durationText, style = MaterialTheme.typography.labelMedium, color = NeonMagenta, fontWeight = FontWeight.Bold)
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
                                                if (isFixed) updateEndHourFromDuration()
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
                                    if (isFixed) updateEndHourFromDuration()
                                },
                                label = { Text("Minutos libres (ej. 90, 600, 1440)", color = TextSecondary, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonMagenta)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Prioridad:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            listOf(1 to "Baja", 2 to "Media", 3 to "Alta").forEach { (p, label) ->
                                FilterChip(
                                    selected = priority == p,
                                    onClick = { priority = p },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when(p) { 3 -> NeonRed; 2 -> NeonAmber; else -> NeonGreen }.copy(alpha = 0.2f),
                                        selectedLabelColor = when(p) { 3 -> NeonRed; 2 -> NeonAmber; else -> NeonGreen }
                                    )
                                )
                            }
                        }
                    }
                }

                item { Divider(color = SurfaceVariantDark) }

                // SECCIÓN 2: ¿CUÁNDO?
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionLabel("¿CUÁNDO?")
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delegar día a Hermes", color = TextPrimary)
                            Switch(checked = letHermesChooseDate, onCheckedChange = { letHermesChooseDate = it; if(it) isFixed = false }, colors = SwitchDefaults.colors(checkedTrackColor = NeonCyan))
                        }

                        if (!letHermesChooseDate) {
                            OutlinedButton(
                                onClick = { showDatePickerInDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(taskDate.time).replaceFirstChar { it.uppercase() }, color = TextPrimary)
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = if(isFixed) NeonAmber else TextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Fijar hora exacta", color = if(isFixed) NeonAmber else TextPrimary)
                                }
                                Switch(checked = isFixed, onCheckedChange = { isFixed = it }, colors = SwitchDefaults.colors(checkedTrackColor = NeonAmber))
                            }

                            if (isFixed) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Incluir hora de fin", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                    Checkbox(checked = hasEndTime, onCheckedChange = { hasEndTime = it }, colors = CheckboxDefaults.colors(checkedColor = NeonMagenta))
                                }

                                var showStartT by remember { mutableStateOf(false) }
                                var showEndT by remember { mutableStateOf(false) }
                                val sState = rememberTimePickerState(hour, minute, true)
                                val eState = rememberTimePickerState(endHour, endMinute, true)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Inicio", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                                        OutlinedButton(onClick = { showStartT = true }, modifier = Modifier.fillMaxWidth()) {
                                            Text("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
                                        }
                                    }
                                    if (hasEndTime) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Fin", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                                            OutlinedButton(onClick = { showEndT = true }, modifier = Modifier.fillMaxWidth()) {
                                                Text("${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}")
                                            }
                                        }
                                    }
                                }
                                if(showStartT) TimePickerDialog(onDismissRequest = { showStartT = false }, confirmButton = { TextButton(onClick = { hour = sState.hour; minute = sState.minute; updateDurationFromHours(); showStartT = false }) { Text("OK") } }) { TimePicker(sState) }
                                if(showEndT) TimePickerDialog(onDismissRequest = { showEndT = false }, confirmButton = { TextButton(onClick = { endHour = eState.hour; endMinute = eState.minute; updateDurationFromHours(); showEndT = false }) { Text("OK") } }) { TimePicker(eState) }
                            }
                        } else {
                            // Hermes elige: fecha disponible desde y hasta (deadline)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Disponible a partir del día", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                OutlinedButton(
                                    onClick = { showAvailableFromDatePicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(availableFromDate.time).replaceFirstChar { it.uppercase() }, color = TextPrimary)
                                }

                                Text("Fecha límite (opcional)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                OutlinedButton(
                                    onClick = { showDeadlinePicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.3f))
                                ) {
                                    val dStr = deadlineDate?.let { SimpleDateFormat("d 'de' MMMM", Locale("es", "ES")).format(it.time) } ?: "Sin límite (máx 14 días)"
                                    Text(dStr, color = if(deadlineDate != null) TextPrimary else TextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        SectionLabel("NOTIFICACIÓN Y AVISO")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf(
                                0 to "Sin aviso", 5 to "5m", 15 to "15m", 30 to "30m",
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
                    }
                }

                item { Divider(color = SurfaceVariantDark) }

                // SECCIÓN 3: REPETICIÓN
                item {
                    var showRepetition by remember { mutableStateOf(false) }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { showRepetition = !showRepetition }) {
                            SectionLabel("OPCIONES DE REPETICIÓN")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(if(showRepetition) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                        
                        AnimatedVisibility(visible = showRepetition) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    daysOfWeekList.forEach { (code, label) ->
                                        val isSelected = code in selectedDays
                                        Box(
                                            modifier = Modifier.size(34.dp).background(if (isSelected) NeonCyan else SurfaceVariantDark, RoundedCornerShape(8.dp))
                                                .clickable { selectedDays = if (isSelected) selectedDays - code else selectedDays + code },
                                            contentAlignment = Alignment.Center
                                        ) { Text(label, color = if (isSelected) Color.Black else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Repetir durante", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { if (numWeeks > 1) numWeeks-- }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Remove, contentDescription = null, tint = TextPrimary) }
                                        Text(numWeeks.toString(), color = NeonCyan, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                        IconButton(onClick = { if (numWeeks < 12) numWeeks++ }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Add, contentDescription = null, tint = TextPrimary) }
                                        Text("semanas", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(start = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalStart: Long?
                    val finalEnd: Long?
                    if (!letHermesChooseDate && isFixed) {
                        val cal = taskDate.clone() as Calendar
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        finalStart = cal.timeInMillis
                        finalEnd = if (hasEndTime) {
                            val endCal = taskDate.clone() as Calendar
                            endCal.set(Calendar.HOUR_OF_DAY, endHour)
                            endCal.set(Calendar.MINUTE, endMinute)
                            if (endCal.before(cal)) endCal.add(Calendar.DAY_OF_YEAR, 1)
                            endCal.timeInMillis
                        } else {
                            null
                        }
                    } else if (!letHermesChooseDate) {
                        // Fecha concreta pero flexible (sin hora fija)
                        val cal = taskDate.clone() as Calendar
                        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                        finalStart = cal.timeInMillis // Se marcará como flexible el motor buscará este día
                        finalEnd = null
                    } else {
                        // Hermes elige el día a partir de la fecha disponible seleccionada
                        val cal = availableFromDate.clone() as Calendar
                        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                        finalStart = cal.timeInMillis
                        finalEnd = null
                    }

                    onSave(
                        TaskEntity(
                            title = title,
                            description = description.ifBlank { null },
                            durationMinutes = duration,
                            reminderLeadMinutes = reminderLeadMinutes,
                            priority = priority,
                            isFixed = isFixed && !letHermesChooseDate,
                            scheduledStart = finalStart,
                            scheduledEnd = finalEnd,
                            deadline = deadlineDate?.timeInMillis
                        ),
                        selectedDays,
                        numWeeks
                    )
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) { Text("Guardar", color = Color.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    // Pickers externos
    if (showDatePickerInDialog) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = taskDate.timeInMillis)
        DatePickerDialog(onDismissRequest = { showDatePickerInDialog = false }, confirmButton = {
            TextButton(onClick = { dpState.selectedDateMillis?.let { taskDate = Calendar.getInstance().apply { timeInMillis = it } }; showDatePickerInDialog = false }) { Text("OK") }
        }) { DatePicker(dpState) }
    }
    if (showAvailableFromDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = availableFromDate.timeInMillis)
        DatePickerDialog(onDismissRequest = { showAvailableFromDatePicker = false }, confirmButton = {
            TextButton(onClick = { dpState.selectedDateMillis?.let { availableFromDate = Calendar.getInstance().apply { timeInMillis = it } }; showAvailableFromDatePicker = false }) { Text("OK") }
        }) { DatePicker(dpState) }
    }
    if (showDeadlinePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = deadlineDate?.timeInMillis ?: (System.currentTimeMillis() + 86400000 * 7))
        DatePickerDialog(onDismissRequest = { showDeadlinePicker = false }, confirmButton = {
            TextButton(onClick = { dpState.selectedDateMillis?.let { deadlineDate = Calendar.getInstance().apply { timeInMillis = it } }; showDeadlinePicker = false }) { Text("OK") }
        }, dismissButton = { TextButton(onClick = { deadlineDate = null; showDeadlinePicker = false }) { Text("Quitar Límite") } }) { DatePicker(dpState) }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
}
