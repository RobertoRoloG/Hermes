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
            .filter { it.isFixed && (it.scheduledStart != null) && (it.scheduledEnd != null) }
            .map { TimeSlot(it.scheduledStart!!, it.scheduledEnd!!) }
            .filter { it.overlapsWith(TimeSlot(startOfDayRaw, endOfDay)) }
            .sortedBy { it.startTimestamp }
            .toList()

        val gaps = mutableListOf<TimeSlot>()
        var currentPointer = startOfDay

        for (fixedSlot in sortedFixedSlots) {
            val fixedStart = fixedSlot.startTimestamp.coerceAtLeast(startOfDay)
            val fixedEnd = fixedSlot.endTimestamp.coerceAtMost(endOfDay)

            if (fixedStart > currentPointer) {
                gaps.add(TimeSlot(currentPointer, fixedStart))
            }
            currentPointer = currentPointer.coerceAtLeast(fixedEnd)
        }

        if (currentPointer < endOfDay) {
            gaps.add(TimeSlot(currentPointer, endOfDay))
        }

        return gaps
    }

    /**
     * Asigna automáticamente una tarea flexible en el primer hueco disponible
     * que respete la duración requerida y el plazo límite (deadline).
     */
    fun autoScheduleFlexibleTask(
        task: TaskEntity,
        startSearchDate: Calendar,
        maxSearchDays: Int,
        existingFixedTasks: List<TaskEntity>
    ): ScheduleResult {
        require(!task.isFixed) { "El motor de auto-programación requiere una tarea flexible." }

        val taskDurationMs = task.durationMinutes * 60 * 1000L
        val searchCalendar = startSearchDate.clone() as Calendar

        repeat(maxSearchDays) {
            val currentDayGaps = calculateAvailableGaps(searchCalendar, existingFixedTasks)

            for (gap in currentDayGaps) {
                val candidateStart = gap.startTimestamp
                val candidateEnd = candidateStart + taskDurationMs

                val fitsInGap = candidateEnd <= gap.endTimestamp
                val respectsDeadline = task.deadline == null || candidateEnd <= task.deadline

                if (fitsInGap && respectsDeadline) {
                    val assignedSlot = TimeSlot(candidateStart, candidateEnd)
                    val scheduledTask = task.copy(
                        scheduledStart = candidateStart,
                        scheduledEnd = candidateEnd,
                        isAutoScheduled = true
                    )
                    return ScheduleResult.Success(scheduledTask, assignedSlot)
                }
            }

            // Avanzar al siguiente día
            searchCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return ScheduleResult.Failure(
            "No se encontró un hueco suficiente de ${task.durationMinutes} min antes del plazo límite en $maxSearchDays días."
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
