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

    // Formulario Nueva Tarea
    var taskTitle by remember { mutableStateOf("") }
    var hourText by remember { mutableStateOf("09") }
    var minuteText by remember { mutableStateOf("00") }

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
                dayTasks.filter { it.scheduledStart != null && it.scheduledEnd != null }
                    .sortedBy { it.scheduledStart }
            }
            
            val unscheduledTasks = remember(dayTasks) {
                dayTasks.filter { it.scheduledStart == null || it.scheduledEnd == null }
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
            onDismiss = { showCreateModal = false },
            onSave = { rawTask, days, weeks ->
                if (days.isNotEmpty()) {
                    viewModel.addTasksForDaysOfWeek(
                        baseTask = rawTask, 
                        selectedDaysOfWeek = days, 
                        startCalendar = selectedCalendar, 
                        fixedStartHour = hourText.toInt(), 
                        fixedStartMinute = minuteText.toInt(),
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
fun CreateTaskDialog(onDismiss: () -> Unit, onSave: (TaskEntity, Set<Int>, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(30) }
    var priority by remember { mutableIntStateOf(1) }
    var isFixed by remember { mutableStateOf(false) }
    var hour by remember { mutableIntStateOf(9) }
    var minute by remember { mutableIntStateOf(0) }
    
    // Nueva lógica para hora de fin
    var endHour by remember { mutableIntStateOf(9) }
    var endMinute by remember { mutableIntStateOf(30) }
    
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var numWeeks by remember { mutableIntStateOf(1) }

    val daysOfWeekList = listOf(
        Calendar.MONDAY to "L",
        Calendar.TUESDAY to "M",
        Calendar.WEDNESDAY to "X",
        Calendar.THURSDAY to "J",
        Calendar.FRIDAY to "V",
        Calendar.SATURDAY to "S",
        Calendar.SUNDAY to "D"
    )

    // Sincronizar duración si cambian horas o viceversa
    fun updateDurationFromHours() {
        val startTotal = hour * 60 + minute
        var endTotal = endHour * 60 + endMinute
        if (endTotal <= startTotal) {
            endTotal += 24 * 60 // Día siguiente
        }
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
        title = { Text("Nueva Tarea", color = NeonCyan) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título de la tarea") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SurfaceVariantDark
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isFixed) Icons.Default.Lock else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isFixed) NeonAmber else NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isFixed) "Hora Fija" else "Auto CSP Flexible",
                                color = if (isFixed) NeonAmber else NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = isFixed,
                            onCheckedChange = { isFixed = it },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = NeonAmber,
                                uncheckedTrackColor = NeonCyan
                            )
                        )
                    }
                }

                if (isFixed) {
                    item {
                        var showStartTimePicker by remember { mutableStateOf(false) }
                        var showEndTimePicker by remember { mutableStateOf(false) }
                        
                        val startTimePickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
                        val endTimePickerState = rememberTimePickerState(initialHour = endHour, initialMinute = endMinute, is24Hour = true)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Inicio:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    OutlinedButton(
                                        onClick = { showStartTimePicker = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                                    ) {
                                        Text("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}", color = TextPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Fin:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    OutlinedButton(
                                        onClick = { showEndTimePicker = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.5f))
                                    ) {
                                        Text("${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}", color = TextPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (showStartTimePicker) {
                                TimePickerDialog(
                                    onDismissRequest = { showStartTimePicker = false },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            hour = startTimePickerState.hour
                                            minute = startTimePickerState.minute
                                            updateDurationFromHours()
                                            showStartTimePicker = false
                                        }) { Text("OK", color = NeonCyan) }
                                    }
                                ) { TimePicker(state = startTimePickerState) }
                            }
                            if (showEndTimePicker) {
                                TimePickerDialog(
                                    onDismissRequest = { showEndTimePicker = false },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            endHour = endTimePickerState.hour
                                            endMinute = endTimePickerState.minute
                                            updateDurationFromHours()
                                            showEndTimePicker = false
                                        }) { Text("OK", color = NeonCyan) }
                                    }
                                ) { TimePicker(state = endTimePickerState) }
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text("Días de la semana:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            daysOfWeekList.forEach { (code, label) ->
                                val isSelected = code in selectedDays
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (isSelected) NeonCyan else SurfaceVariantDark,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedDays = if (isSelected) selectedDays - code else selectedDays + code
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, color = if (isSelected) Color.Black else TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text("Repetir durante (semanas):", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            IconButton(
                                onClick = { if (numWeeks > 1) numWeeks-- },
                                modifier = Modifier.size(32.dp).background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                            ) { Icon(Icons.Default.Remove, contentDescription = "Menos", tint = TextPrimary) }
                            Text(text = numWeeks.toString(), style = MaterialTheme.typography.titleLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { if (numWeeks < 12) numWeeks++ },
                                modifier = Modifier.size(32.dp).background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                            ) { Icon(Icons.Default.Add, contentDescription = "Más", tint = TextPrimary) }
                        }
                    }
                }

                item {
                    Text("Prioridad", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1 to "Baja", 2 to "Media", 3 to "Alta").forEach { (p, label) ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                item {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Duración", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("${duration}m", style = MaterialTheme.typography.labelSmall, color = NeonMagenta, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = duration.toFloat(),
                            onValueChange = { 
                                duration = ((it / 5f).roundToInt() * 5).coerceAtLeast(5)
                                if (isFixed) updateEndHourFromDuration()
                            },
                            valueRange = 5f..480f,
                            colors = SliderDefaults.colors(thumbColor = NeonMagenta, activeTrackColor = NeonMagenta)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(15, 30, 45, 60, 90, 120)) { d ->
                                FilterChip(
                                    selected = duration == d,
                                    onClick = { 
                                        duration = d
                                        if (isFixed) updateEndHourFromDuration()
                                    },
                                    label = { Text("${d}m") }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = Calendar.getInstance()
                    var start: Long? = null
                    var end: Long? = null
                    if (isFixed) {
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        start = cal.timeInMillis
                        end = start + (duration * 60 * 1000L)
                    }
                    onSave(
                        TaskEntity(
                            title = title,
                            durationMinutes = duration,
                            priority = priority,
                            isFixed = isFixed,
                            scheduledStart = start,
                            scheduledEnd = end
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
}
