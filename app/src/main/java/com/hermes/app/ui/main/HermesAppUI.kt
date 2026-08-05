package com.hermes.app.ui.main

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.app.data.local.HermesDatabase
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.domain.RoleItem
import com.hermes.app.domain.RoleManager
import com.hermes.app.ui.theme.*
import com.hermes.app.utils.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HermesAppUI(
    database: HermesDatabase,
    roleManager: RoleManager,
    onUpdateAssistantName: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf<List<TaskEntity>>(emptyList()) }
    var assistantName by remember { mutableStateOf(roleManager.getAssistantName()) }
    var activeRole by remember { mutableStateOf(roleManager.getActiveRole()) }
    var allRoles by remember { mutableStateOf(roleManager.getAllRoles()) }

    var showNameDialog by remember { mutableStateOf(false) }
    var showCreateRoleDialog by remember { mutableStateOf(false) }

    // Formulario de Tarea
    var taskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableIntStateOf(1) } // 1: Baja, 2: Media, 3: Alta
    var selectedDuration by remember { mutableIntStateOf(30) } // minutos
    var selectedLeadMinutes by remember { mutableIntStateOf(15) } // minutos de antelación
    var isFixedTask by remember { mutableStateOf(false) }

    // Configuración para Tarea Fija (Hora de inicio / fin)
    var fixedStartHour by remember { mutableIntStateOf(9) }
    var fixedStartMinute by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        NotificationScheduler.createNotificationChannel(context)
        database.taskDao().getAllTasks().collect { taskList ->
            tasks = taskList
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
                    Column {
                        Text(
                            text = assistantName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "Rol activo: ${activeRole.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonMagenta
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showNameDialog = true }) {
                        Text("⚙️", fontSize = 18.sp)
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // SECCIÓN DE ROLES DEL ASISTENTE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personalidad del Asistente",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = { showCreateRoleDialog = true }) {
                    Text("+ Crear Rol", color = NeonMagenta, fontWeight = FontWeight.Bold)
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(allRoles) { role ->
                    val isSelected = activeRole.id == role.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            activeRole = role
                            roleManager.setActiveRole(role.id)
                        },
                        label = {
                            Text(
                                text = role.displayName,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            containerColor = SurfaceDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = SurfaceVariantDark,
                            selectedBorderColor = NeonCyan,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 2.dp
                        )
                    )
                }
            }

            // FORMULARIO AVANZADO DE NUEVA TAREA
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚡ Nueva Tarea",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Título / Descripción de la tarea", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SurfaceVariantDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // SELECCIÓN DE PRIORIDAD
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Prioridad: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        listOf(1 to "Baja 🟢", 2 to "Media 🟡", 3 to "Alta 🔴").forEach { (level, label) ->
                            val selected = selectedPriority == level
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .background(
                                        color = if (selected) when (level) {
                                            3 -> NeonRed
                                            2 -> NeonAmber
                                            else -> NeonGreen
                                        } else SurfaceVariantDark,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedPriority = level }
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // SELECCIÓN DE DURACIÓN
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Duración: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf(15, 30, 45, 60, 90, 120)) { mins ->
                                val selected = selectedDuration == mins
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (selected) NeonMagenta else SurfaceVariantDark,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedDuration = mins }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${mins}m",
                                        color = if (selected) Color.Black else TextPrimary,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // SELECCIÓN DE ANTELACIÓN DE AVISO (PRE-ALERTA)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Avisarme antes: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf(0, 5, 15, 30, 60)) { leadMins ->
                                val selected = selectedLeadMinutes == leadMins
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (selected) NeonCyan else SurfaceVariantDark,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedLeadMinutes = leadMins }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (leadMins == 0) "Al inicio" else "${leadMins}m antes",
                                        color = if (selected) Color.Black else TextPrimary,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // TIPO DE TAREA: FLEXIBLE VS FIJA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isFixedTask) "Horario Fijo 📌" else "Auto CSP Flexible 🤖",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isFixedTask) NeonAmber else NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = isFixedTask,
                            onCheckedChange = { isFixedTask = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = NeonAmber,
                                uncheckedThumbColor = Color.Black,
                                uncheckedTrackColor = NeonCyan
                            )
                        )
                    }

                    AnimatedVisibility(visible = isFixedTask) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "Hora de Inicio (Hoy):",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                OutlinedTextField(
                                    value = fixedStartHour.toString(),
                                    onValueChange = { fixedStartHour = it.toIntOrNull()?.coerceIn(0, 23) ?: 9 },
                                    label = { Text("Hora (0-23)", color = TextSecondary) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                OutlinedTextField(
                                    value = fixedStartMinute.toString(),
                                    onValueChange = { fixedStartMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: 0 },
                                    label = { Text("Minuto (0-59)", color = TextSecondary) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                var startMs: Long? = null
                                var endMs: Long? = null

                                if (isFixedTask) {
                                    val cal = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, fixedStartHour)
                                        set(Calendar.MINUTE, fixedStartMinute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    startMs = cal.timeInMillis
                                    endMs = startMs + (selectedDuration * 60 * 1000L)
                                }

                                val rawTask = TaskEntity(
                                    title = taskTitle.trim(),
                                    priority = selectedPriority,
                                    durationMinutes = selectedDuration,
                                    reminderLeadMinutes = selectedLeadMinutes,
                                    isFixed = isFixedTask,
                                    scheduledStart = startMs,
                                    scheduledEnd = endMs
                                )

                                val contextualized = roleManager.applyRoleContextToTask(rawTask)
                                scope.launch(Dispatchers.IO) {
                                    val insertedId = database.taskDao().insertTask(contextualized)
                                    val savedTask = contextualized.copy(id = insertedId)

                                    if (savedTask.scheduledStart != null) {
                                        NotificationScheduler.scheduleTaskNotification(context, savedTask)
                                    }

                                    val aiResponse = roleManager.generateDynamicNotificationMessage(savedTask)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, aiResponse, Toast.LENGTH_LONG).show()
                                    }
                                }
                                taskTitle = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar Tarea a Hermes", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "📋 Tareas Registradas (${tasks.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCardItem(
                        task = task,
                        onToggleComplete = { isDone ->
                            scope.launch(Dispatchers.IO) {
                                database.taskDao().updateTaskStatus(task.id, isDone)
                                if (isDone) {
                                    NotificationScheduler.cancelTaskNotification(context, task.id)
                                }
                            }
                        },
                        onDelete = {
                            scope.launch(Dispatchers.IO) {
                                database.taskDao().deleteTask(task)
                                NotificationScheduler.cancelTaskNotification(context, task.id)
                            }
                        }
                    )
                }
            }
        }
    }

    // DIÁLOGO PARA CAMBIAR NOMBRE DEL ASISTENTE
    if (showNameDialog) {
        var tempName by remember { mutableStateOf(assistantName) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Nombre del Asistente", color = NeonCyan) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Nombre") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            assistantName = tempName.trim()
                            onUpdateAssistantName(assistantName)
                        }
                        showNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Guardar", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // DIÁLOGO PARA CREAR ROL PERSONALIZADO
    if (showCreateRoleDialog) {
        var roleName by remember { mutableStateOf("") }
        var roleDesc by remember { mutableStateOf("") }
        var priorityBoost by remember { mutableIntStateOf(0) }
        var customPhrase by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateRoleDialog = false },
            containerColor = SurfaceDark,
            title = { Text("⚡ Crear Nuevo Rol", color = NeonMagenta) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = roleName,
                        onValueChange = { roleName = it },
                        label = { Text("Nombre del Rol (ej. IA Mentor)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = roleDesc,
                        onValueChange = { roleDesc = it },
                        label = { Text("Descripción breve", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = customPhrase,
                        onValueChange = { customPhrase = it },
                        label = { Text("Frase al agendar tarea", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        )
                    )
                    Text("Impacto en prioridad:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(-1 to "Relajar (-1)", 0 to "Neutral (0)", 1 to "Priorizar (+1)").forEach { (boost, label) ->
                            FilterChip(
                                selected = priorityBoost == boost,
                                onClick = { priorityBoost = boost },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (roleName.isNotBlank()) {
                            val newRole = roleManager.addCustomRole(
                                name = roleName.trim(),
                                description = roleDesc.trim(),
                                priorityBoost = priorityBoost,
                                phrase = customPhrase.ifBlank { "Tarea agendada:" }
                            )
                            allRoles = roleManager.getAllRoles()
                            activeRole = newRole
                            roleManager.setActiveRole(newRole.id)
                        }
                        showCreateRoleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta)
                ) {
                    Text("Crear Rol", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoleDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun TaskCardItem(
    task: TaskEntity,
    onToggleComplete: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (task.priority) {
        3 -> NeonRed
        2 -> NeonAmber
        else -> NeonGreen
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceVariantDark, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onToggleComplete,
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonCyan,
                    checkmarkColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) TextSecondary else TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de Prioridad
                    Surface(
                        color = priorityColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, priorityColor)
                    ) {
                        Text(
                            text = when (task.priority) {
                                3 -> "P3 (Alta)"
                                2 -> "P2 (Media)"
                                else -> "P1 (Baja)"
                            },
                            color = priorityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    // Badge de Duración
                    Surface(
                        color = NeonMagenta.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${task.durationMinutes} min",
                            color = NeonMagenta,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    // Badge Antelación
                    Surface(
                        color = NeonCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "⏰ -${task.reminderLeadMinutes}m",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    // Badge Tipo (Fija / Flexible)
                    Surface(
                        color = if (task.isFixed) NeonAmber.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (task.isFixed) "FIJA" else "AUTO CSP",
                            color = if (task.isFixed) NeonAmber else NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Text("🗑️", fontSize = 16.sp)
            }
        }
    }
}
