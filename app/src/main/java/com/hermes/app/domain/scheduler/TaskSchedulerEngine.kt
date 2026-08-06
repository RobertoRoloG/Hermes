package com.hermes.app.domain.scheduler

import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.domain.model.TimeSlot
import java.util.Calendar

sealed class ScheduleResult {
    data class Success(val scheduledTask: TaskEntity, val assignedSlot: TimeSlot) : ScheduleResult()
    data class Failure(val reason: String) : ScheduleResult()
}

class TaskSchedulerEngine(
    private val workDayStartHour: Int = 8,
    private val workDayEndHour: Int = 20,
) {

    /**
     * Calcula los huecos libres (Gaps) dentro del horario laboral de una jornada concreta,
     * restando los bloques de tareas fijas.
     */
    fun calculateAvailableGaps(
        dayCalendar: Calendar,
        fixedTasks: List<TaskEntity>
    ): List<TimeSlot> {
        val now = System.currentTimeMillis()
        val todayCal = Calendar.getInstance()
        val isToday = dayCalendar.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                dayCalendar.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)

        val startOfDayRaw = (dayCalendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, workDayStartHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfDay = if (isToday) maxOf(startOfDayRaw, now) else startOfDayRaw

        val endOfDay = (dayCalendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, workDayEndHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (startOfDay >= endOfDay) {
            return emptyList()
        }

        val sortedFixedSlots = fixedTasks.asSequence()
            .filter { it.scheduledStart != null }
            .map { task ->
                val start = task.scheduledStart!!
                val end = task.scheduledEnd ?: (start + (task.durationMinutes * 60 * 1000L))
                TimeSlot(start, end)
            }
            .filter { it.overlapsWith(TimeSlot(startOfDayRaw, endOfDay)) }
            .sortedBy { it.startTimestamp }
            .toList()

        val gaps = mutableListOf<TimeSlot>()
        var currentPointer = startOfDay

        for (fixedSlot in sortedFixedSlots) {
            val fixedStart = fixedSlot.startTimestamp.coerceAtLeast(startOfDay)
            
            // Calculamos el buffer que esta tarea "ocupa" al terminar
            val taskDurationMin = (fixedSlot.endTimestamp - fixedSlot.startTimestamp) / (60 * 1000)
            val bufferMs = if (taskDurationMin >= 60) 15 * 60 * 1000L else 5 * 60 * 1000L
            val fixedEndWithBuffer = (fixedSlot.endTimestamp + bufferMs).coerceAtMost(endOfDay)

            if (fixedStart > currentPointer) {
                gaps.add(TimeSlot(currentPointer, fixedStart))
            }
            currentPointer = currentPointer.coerceAtLeast(fixedEndWithBuffer)
        }

        if (currentPointer < endOfDay) {
            gaps.add(TimeSlot(currentPointer, endOfDay))
        }

        return gaps
    }

    fun autoScheduleFlexibleTask(
        task: TaskEntity,
        startSearchDate: Calendar,
        maxSearchDays: Int,
        existingFixedTasks: List<TaskEntity>
    ): ScheduleResult {
        require(!task.isFixed) { "El motor de auto-programación requiere una tarea flexible." }

        val taskDurationMs = task.durationMinutes * 60 * 1000L
        
        // Si la tarea tiene un inicio pre-marcado (p.ej. por repetición en un día), usamos ese día como inicio
        // Si es totalmente libre (null), usamos la fecha de búsqueda global (hoy)
        val searchCalendar = (task.scheduledStart?.let { 
            Calendar.getInstance().apply { timeInMillis = it }
        } ?: startSearchDate).clone() as Calendar

        // Si viene con fecha pre-marcada, solemos buscar solo en ese día. 
        // Si es null, buscamos en toda la ventana de días.
        val limitDays = if (task.scheduledStart != null && !task.isAutoScheduled) 1 else maxSearchDays

        repeat(limitDays) {
            val currentDayGaps = calculateAvailableGaps(searchCalendar, existingFixedTasks)
            
            // Lógica Best-Fit: Encontrar el hueco que mejor se ajuste (menor desperdicio)
            var bestGap: TimeSlot? = null
            var minWaste = Long.MAX_VALUE

            for (gap in currentDayGaps) {
                val gapDuration = gap.endTimestamp - gap.startTimestamp
                
                // Comprobamos si cabe la tarea (el buffer ya está implícito en la reducción de los gaps)
                if (gapDuration >= taskDurationMs) {
                    val waste = gapDuration - taskDurationMs
                    
                    val candidateEnd = gap.startTimestamp + taskDurationMs
                    val respectsDeadline = task.deadline == null || candidateEnd <= task.deadline

                    if (respectsDeadline && waste < minWaste) {
                        minWaste = waste
                        bestGap = gap
                    }
                }
            }

            if (bestGap != null) {
                val assignedStart = bestGap.startTimestamp
                val assignedEnd = assignedStart + taskDurationMs
                
                val scheduledTask = task.copy(
                    scheduledStart = assignedStart,
                    scheduledEnd = assignedEnd,
                    isAutoScheduled = true
                )
                // El assignedSlot aquí es meramente informativo en el resultado actual
                return ScheduleResult.Success(scheduledTask, TimeSlot(assignedStart, assignedEnd))
            }

            // Avanzar al siguiente día
            searchCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return ScheduleResult.Failure(
            "No se encontró un hueco suficiente de ${task.durationMinutes} min en $maxSearchDays días."
        )
    }

    /**
     * Planifica en lote una lista de tareas flexibles según su prioridad.
     */
    fun batchScheduleFlexibleTasks(
        flexibleTasks: List<TaskEntity>,
        startSearchDate: Calendar,
        maxSearchDays: Int,
        fixedTasks: List<TaskEntity>
    ): List<TaskEntity> {
        val currentFixedList = fixedTasks.toMutableList()
        val scheduledResults = mutableListOf<TaskEntity>()

        // Ordenar por prioridad descendente y luego por deadline ascendente
        val sortedTasks = flexibleTasks.sortedWith(
            compareByDescending<TaskEntity> { it.priority }
                .thenBy { it.deadline ?: Long.MAX_VALUE }
        )

        for (task in sortedTasks) {
            val result = autoScheduleFlexibleTask(
                task = task,
                startSearchDate = startSearchDate,
                maxSearchDays = maxSearchDays,
                existingFixedTasks = currentFixedList
            )

            if (result is ScheduleResult.Success) {
                scheduledResults.add(result.scheduledTask)
                currentFixedList.add(result.scheduledTask) // Tratar como bloque ocupado para las siguientes tareas
            } else {
                scheduledResults.add(task) // Sin cambios
            }
        }

        return scheduledResults
    }
}
