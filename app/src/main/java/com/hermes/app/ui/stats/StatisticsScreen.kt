package com.hermes.app.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.ui.theme.*
import java.util.Calendar

import androidx.compose.ui.platform.LocalContext
import com.hermes.app.utils.ExcelExporter

enum class StatsTimeRange {
    DIARIO, SEMANAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    tasks: List<TaskEntity>,
    roleManager: com.hermes.app.domain.RoleManager
) {
    val context = LocalContext.current
    var selectedRange by remember { mutableStateOf(StatsTimeRange.SEMANAL) }

    // FILTRADO DINÁMICO DE TAREAS SEGÚN EL RANGO SELECCIONADO (DIARIO O SEMANAL)
    val filteredTasks = remember(tasks, selectedRange) {
        val now = Calendar.getInstance()
        
        val startOfRange = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            when (selectedRange) {
                StatsTimeRange.DIARIO -> { /* Ya es hoy */ }
                StatsTimeRange.SEMANAL -> {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }
            }
        }.timeInMillis

        val endOfRange = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
            
            when (selectedRange) {
                StatsTimeRange.DIARIO -> { /* Ya es hoy */ }
                StatsTimeRange.SEMANAL -> {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    add(Calendar.DAY_OF_YEAR, 6)
                }
            }
        }.timeInMillis

        tasks.filter { task ->
            val taskTime = task.scheduledStart ?: task.createdAt
            taskTime in startOfRange..endOfRange
        }
    }

    val totalTasks = filteredTasks.size
    val completedTasks = filteredTasks.count { it.isCompleted }
    val pendingTasks = totalTasks - completedTasks
    val completionRate = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks.toFloat() * 100).toInt() else 0

    val highPriorityCount = filteredTasks.count { it.priority == 3 }
    val highPriorityCompleted = filteredTasks.count { it.priority == 3 && it.isCompleted }

    val mediumPriorityCount = filteredTasks.count { it.priority == 2 }
    val mediumPriorityCompleted = filteredTasks.count { it.priority == 2 && it.isCompleted }

    val lowPriorityCount = filteredTasks.count { it.priority == 1 }
    val lowPriorityCompleted = filteredTasks.count { it.priority == 1 && it.isCompleted }

    val dynamicRolesList = (roleManager.getAllRoles().map { it.displayName } + tasks.map { it.createdRole }.filter { it.isNotBlank() }).distinct()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = NeonCyan
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Estadísticas de Rendimiento", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SELECTOR DE RANGO TEMPORAL (DIARIO, SEMANAL) Y BOTÓN EXPORTAR EXCEL
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            StatsTimeRange.DIARIO to "Diario",
                            StatsTimeRange.SEMANAL to "Semanal"
                        ).forEach { (range, label) ->
                            val isSelected = selectedRange == range
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected) NeonCyan else SurfaceDark,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonCyan else SurfaceVariantDark,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedRange = range }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else TextPrimary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            ExcelExporter.exportTasksToExcel(context, filteredTasks, selectedRange)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Exportar planning a Excel", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // TARJETAS DE MÉTRICAS RÁPIDAS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Completadas",
                        value = "$completedTasks",
                        color = NeonGreen,
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Pendientes",
                        value = "$pendingTasks",
                        color = NeonMagenta,
                        icon = Icons.Default.PendingActions,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Efectividad",
                        value = "$completionRate%",
                        color = NeonCyan,
                        icon = Icons.Default.Star,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // GRÁFICO DE ROSCO / ANILLO DE CUMPLIMIENTO (DONUT CHART)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Cumplimiento (${selectedRange.name.lowercase().replaceFirstChar { it.uppercase() }})",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(160.dp)
                        ) {
                            Canvas(modifier = Modifier.size(150.dp)) {
                                val strokeWidth = 22.dp.toPx()
                                // Anillo de fondo (Pendientes)
                                drawArc(
                                    color = SurfaceVariantDark,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                                // Anillo de progreso (Completadas)
                                val sweep = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks.toFloat()) * 360f else 0f
                                drawArc(
                                    color = NeonGreen,
                                    startAngle = -90f,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$completionRate%",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen
                                )
                                Text(
                                    text = "$completedTasks de $totalTasks completadas",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // DESGLOSE POR PRIORIDAD (P3 Alta, P2 Media, P1 Baja)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Desglose por Prioridad",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        PriorityProgressRow(
                            label = "Alta (P3)",
                            completed = highPriorityCompleted,
                            total = highPriorityCount,
                            color = NeonRed
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        PriorityProgressRow(
                            label = "Media (P2)",
                            completed = mediumPriorityCompleted,
                            total = mediumPriorityCount,
                            color = NeonAmber
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        PriorityProgressRow(
                            label = "Baja (P1)",
                            completed = lowPriorityCompleted,
                            total = lowPriorityCount,
                            color = NeonGreen
                        )
                    }
                }
            }

            // DESGLOSE POR ROL DE ASISTENTE
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Rendimiento según Rol Asignado",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        dynamicRolesList.forEach { roleName ->
                            val roleTasks = filteredTasks.filter { it.createdRole == roleName }
                            val roleTotal = roleTasks.size
                            val roleDone = roleTasks.count { it.isCompleted }
                            val pct = if (roleTotal > 0) (roleDone.toFloat() / roleTotal * 100).toInt() else 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(roleName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text(
                                    text = "$roleDone/$roleTotal ($pct%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun PriorityProgressRow(
    label: String,
    completed: Int,
    total: Int,
    color: Color
) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("$completed/$total", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = SurfaceVariantDark,
            strokeCap = StrokeCap.Round
        )
    }
}
