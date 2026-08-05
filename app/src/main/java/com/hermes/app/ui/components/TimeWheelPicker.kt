package com.hermes.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.app.ui.theme.DarkBackground
import com.hermes.app.ui.theme.NeonCyan
import com.hermes.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun TimeWheelPicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialHour.coerceIn(0, 23)) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute.coerceIn(0, 59)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rueda de Horas (00 - 23)
        NumberWheelColumn(
            range = 0..23,
            selectedNumber = selectedHour,
            onNumberSelected = {
                selectedHour = it
                onTimeSelected(selectedHour, selectedMinute)
            },
            label = "Hora"
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Rueda de Minutos (00 - 59)
        NumberWheelColumn(
            range = 0..59,
            selectedNumber = selectedMinute,
            onNumberSelected = {
                selectedMinute = it
                onTimeSelected(selectedHour, selectedMinute)
            },
            label = "Minuto"
        )
    }
}

@Composable
private fun NumberWheelColumn(
    range: IntRange,
    selectedNumber: Int,
    onNumberSelected: (Int) -> Unit,
    label: String
) {
    val itemsList = range.toList()
    val scope = rememberCoroutineScope()
    val initialIndex = remember(selectedNumber) { itemsList.indexOf(selectedNumber).coerceAtLeast(0) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex
            if (centerIndex in itemsList.indices) {
                onNumberSelected(itemsList[centerIndex])
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(130.dp)
                .background(DarkBackground, RoundedCornerShape(12.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Franja de selección central
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .border(1.dp, NeonCyan, RoundedCornerShape(6.dp))
            )

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = 45.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                items(itemsList.size) { index ->
                    val num = itemsList[index]
                    val isSelected = num == selectedNumber

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clickable {
                                onNumberSelected(num)
                                scope.launch { listState.animateScrollToItem(index) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = num.toString().padStart(2, '0'),
                            fontSize = if (isSelected) 22.sp else 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NeonCyan else TextSecondary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
