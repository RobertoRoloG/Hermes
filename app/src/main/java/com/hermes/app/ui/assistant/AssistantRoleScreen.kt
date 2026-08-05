package com.hermes.app.ui.assistant

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.app.domain.RoleItem
import com.hermes.app.domain.RoleManager
import com.hermes.app.ui.theme.*
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantRoleScreen(
    roleManager: RoleManager,
    onUpdateAssistantName: (String) -> Unit
) {
    var assistantName by remember { mutableStateOf(roleManager.getAssistantName()) }
    var selectedRoleIds by remember { mutableStateOf(roleManager.getSelectedRoleIds()) }
    var allRoles by remember { mutableStateOf(roleManager.getAllRoles()) }
    var showNameDialog by remember { mutableStateOf(false) }
    var roleToDelete by remember { mutableStateOf<RoleItem?>(null) }

    // Formulario de Creación de Rol
    var newRoleName by remember { mutableStateOf("") }
    var newRoleDesc by remember { mutableStateOf("") }
    var newRolePhrase by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = NeonCyan
                ),
                title = { Text("Personalidad del Asistente", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showNameDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = NeonCyan)
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
            // TARJETA NOMBRE DEL ASISTENTE
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonMagenta.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Nombre del Asistente", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text(assistantName, style = MaterialTheme.typography.titleLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showNameDialog = true },
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonMagenta)
                        ) {
                            Text("Cambiar Nombre", color = NeonMagenta, fontSize = 12.sp)
                        }
                    }
                }
            }

            // SECCIÓN SELECCIÓN MÚLTIPLE DE ROLES
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Roles para Asignación Aleatoria",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Selecciona uno o varios roles. Al crear cada tarea, Hermes elegirá un rol al azar de entre los marcados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            items(allRoles, key = { it.id }) { role ->
                val isSelected = role.id in selectedRoleIds
                val borderColor = if (isSelected) NeonCyan else SurfaceVariantDark
                val containerColor = if (isSelected) SurfaceVariantDark else SurfaceDark

                Card(
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
                        .clickable {
                            roleManager.toggleRoleSelection(role.id)
                            selectedRoleIds = roleManager.getSelectedRoleIds()
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = role.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) NeonCyan else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Surface(
                                        color = NeonCyan,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "ACTIVO (RANDOM)",
                                                color = Color.Black,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (role.id.startsWith("CUSTOM_")) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { roleToDelete = role },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Rol", tint = NeonRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = role.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Frase distintiva: \"${role.customPhrase}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonMagenta
                        )
                    }
                }
            }

            // CREAR NUEVO ROL PERSONALIZADO
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonMagenta.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = NeonMagenta, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Crear un Nuevo Rol Personalizado",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonMagenta,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Define un nuevo rol con su propio tono y personalidad para Gemini AI.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newRoleName,
                            onValueChange = { newRoleName = it },
                            label = { Text("Nombre del Rol (ej. Mentor de Código)", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newRoleDesc,
                            onValueChange = { newRoleDesc = it },
                            label = { Text("Descripción de la personalidad / Tono", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newRolePhrase,
                            onValueChange = { newRolePhrase = it },
                            label = { Text("Frase distintiva de plantilla", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (newRoleName.isNotBlank()) {
                                    val created = roleManager.addCustomRole(
                                        name = newRoleName.trim(),
                                        description = newRoleDesc.trim(),
                                        phrase = newRolePhrase.ifBlank { "Tarea agendada:" }
                                    )
                                    allRoles = roleManager.getAllRoles()
                                    selectedRoleIds = roleManager.getSelectedRoleIds()

                                    newRoleName = ""
                                    newRoleDesc = ""
                                    newRolePhrase = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Crear y Guardar Rol", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN DE BORRADO DE ROL
    roleToDelete?.let { role ->
        AlertDialog(
            onDismissRequest = { roleToDelete = null },
            containerColor = SurfaceDark,
            title = { Text("Confirmar eliminación de rol", color = NeonRed, fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar el rol '${role.displayName}'?", color = TextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        roleManager.deleteRole(role.id)
                        allRoles = roleManager.getAllRoles()
                        selectedRoleIds = roleManager.getSelectedRoleIds()
                        roleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { roleToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // DIÁLOGO EDITAR NOMBRE
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
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
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
}
