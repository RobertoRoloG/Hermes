package com.hermes.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun InteractiveTimelineCard(
    task: TaskEntity,
    timeFormatter: SimpleDateFormat,
    onToggleComplete: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLockTaskAsFixed: () -> Unit,
    onShiftMinutes: (Int) -> Unit
) {
    val startStr = task.scheduledStart?.let { timeFormatter.format(Date(it)) } ?: "--:--"
    val endStr = task.scheduledEnd?.let { timeFormatter.format(Date(it)) } ?: "--:--"

    val priorityColor = when (task.priority) {
        3 -> NeonRed
        2 -> NeonAmber
        else -> NeonGreen
    }

    val badgeColor = when {
        task.isFixed -> NeonRed
        task.isAutoScheduled -> NeonCyan
        else -> NeonMagenta
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (task.isCompleted) SurfaceVariantDark else badgeColor.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) DarkBackground.copy(alpha = 0.6f) else SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(if (task.isCompleted) SurfaceVariantDark else priorityColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = onToggleComplete,
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonCyan,
                                checkmarkColor = Color.Black
                            ),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(Icons.Default.Schedule, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$startStr - $endStr",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) TextSecondary else NeonCyan
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (task.isCompleted) SurfaceVariantDark else badgeColor.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (task.isCompleted) SurfaceVariantDark else badgeColor
                            )
                        ) {
                            Text(
                                text = if (task.isCompleted) "COMPLETADA" else if (task.isFixed) "FIJA" else if (task.isAutoScheduled) "AUTO CSP" else "MANUAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (task.isCompleted) TextSecondary else badgeColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = NeonCyan, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = NeonRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) TextSecondary else TextPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Duración: ${task.durationMinutes}m | Rol: ${task.createdRole}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    if (!task.isCompleted) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { onShiftMinutes(-15) }) {
                                Text("-15m", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            }
                            TextButton(onClick = { onShiftMinutes(15) }) {
                                Text("+15m", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            }
                            if (!task.isFixed) {
                                IconButton(
                                    onClick = onLockTaskAsFixed,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = "Fijar", tint = NeonAmber, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendBadge(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )
    }
}
