package com.hermes.app.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hermes.app.data.local.HermesDatabase
import com.hermes.app.data.local.entity.TaskEntity
import com.hermes.app.domain.RoleManager
import com.hermes.app.domain.scheduler.TaskSchedulerEngine
import com.hermes.app.utils.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    val database = HermesDatabase.getDatabase(context)
    val roleManager = RoleManager(context)
    private val schedulerEngine = TaskSchedulerEngine()

    val allTasks: StateFlow<List<TaskEntity>> = database.taskDao().getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())
    val selectedCalendar: StateFlow<Calendar> = _selectedCalendar.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val dayTasks: StateFlow<List<TaskEntity>> = _selectedCalendar.flatMapLatest { cal ->
        val startMs = (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endMs = (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        database.taskDao().getTasksForDateRange(startMs, endMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedDate(calendar: Calendar) {
        _selectedCalendar.value = calendar
    }

    fun findOverlappingTask(newTaskStart: Long?, durationMinutes: Int, newTaskEnd: Long? = null, ignoreTaskId: Long = 0L): TaskEntity? {
        if (newTaskStart == null) return null
        val calculatedEnd = newTaskEnd ?: (newTaskStart + durationMinutes * 60 * 1000L)
        
        return allTasks.value.find { existing ->
            if (existing.id == ignoreTaskId || existing.isCompleted || existing.scheduledStart == null) {
                false
            } else {
                val existStart = existing.scheduledStart
                val existEnd = existing.scheduledEnd ?: (existStart + existing.durationMinutes * 60 * 1000L)
                newTaskStart < existEnd && calculatedEnd > existStart
            }
        }
    }

    fun addTask(task: TaskEntity) {
        val contextualized = roleManager.applyRoleContextToTask(task)
        viewModelScope.launch(Dispatchers.IO) {
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
    }

    fun addTasksForDaysOfWeek(
        baseTask: TaskEntity,
        selectedDaysOfWeek: Set<Int>,
        startCalendar: Calendar,
        fixedStartHour: Int,
        fixedStartMinute: Int,
        numWeeks: Int = 1
    ) {
        if (selectedDaysOfWeek.isEmpty()) {
            addTask(baseTask)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val createdTasks = mutableListOf<TaskEntity>()
            val totalDays = numWeeks * 7

            for (dayOffset in 0 until totalDays) {
                val currentCal = (startCalendar.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, dayOffset)
                    set(Calendar.HOUR_OF_DAY, fixedStartHour)
                    set(Calendar.MINUTE, fixedStartMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val dayOfWeek = currentCal.get(Calendar.DAY_OF_WEEK)
                if (dayOfWeek in selectedDaysOfWeek) {
                    val startMs: Long?
                    var endMs: Long? = null

                    if (baseTask.isFixed) {
                        startMs = currentCal.timeInMillis
                        endMs = baseTask.scheduledEnd?.let {
                            startMs + (baseTask.durationMinutes * 60 * 1000L)
                        }
                    } else {
                        startMs = currentCal.timeInMillis
                    }

                    val taskForDay = baseTask.copy(
                        id = 0,
                        scheduledStart = startMs,
                        scheduledEnd = endMs,
                        createdAt = System.currentTimeMillis(),
                        isAutoScheduled = false
                    )
                    val contextualized = roleManager.applyRoleContextToTask(taskForDay)
                    val insertedId = database.taskDao().insertTask(contextualized)
                    val savedTask = contextualized.copy(id = insertedId)

                    if (savedTask.scheduledStart != null) {
                        NotificationScheduler.scheduleTaskNotification(context, savedTask)
                    }
                    createdTasks.add(savedTask)
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Se crearon ${createdTasks.size} tareas durante $numWeeks semanas.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun completeTask(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            database.taskDao().updateTaskStatus(taskId, isCompleted)
            if (isCompleted) {
                NotificationScheduler.cancelTaskNotification(context, taskId)
            } else {
                val task = database.taskDao().getTaskById(taskId)
                if (task?.scheduledStart != null) {
                    NotificationScheduler.scheduleTaskNotification(context, task)
                }
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.taskDao().deleteTask(task)
            NotificationScheduler.cancelTaskNotification(context, task.id)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.taskDao().updateTask(task)
            if (task.isCompleted) {
                NotificationScheduler.cancelTaskNotification(context, task.id)
            } else if (task.scheduledStart != null) {
                NotificationScheduler.scheduleTaskNotification(context, task)
            } else {
                NotificationScheduler.cancelTaskNotification(context, task.id)
            }
        }
    }

    fun runAutoScheduler() {
        viewModelScope.launch(Dispatchers.IO) {
            runAutoSchedulerForDate(_selectedCalendar.value, useAI = true)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Tareas re-planificadas con éxito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun runAutoSchedulerForDate(searchDate: Calendar, useAI: Boolean = false) {
        val fixedTasks = database.taskDao().getFixedTasks()
        var flexibleTasks = database.taskDao().getAllFlexibleTasks()

        if (flexibleTasks.isEmpty()) return

        // --- OPTIMIZACIÓN ASISTIDA POR IA ---
        if (useAI) {
            val aiRankedIds = roleManager.getAIRankedTaskIds(flexibleTasks)
            if (aiRankedIds.isNotEmpty()) {
                // Reordenar la lista según lo que dijo Gemini
                val idMap = flexibleTasks.associateBy { it.id }
                flexibleTasks = aiRankedIds.mapNotNull { idMap[it] }
            }
        }

        // El motor usa los límites por defecto (08:00 - 20:00) ya que se eliminó la configuración de UI
        val dynamicEngine = TaskSchedulerEngine()

        // Window de búsqueda de 14 días para mayor flexibilidad
        val scheduledResults = dynamicEngine.batchScheduleFlexibleTasks(
            flexibleTasks = flexibleTasks,
            startSearchDate = searchDate,
            maxSearchDays = 14,
            fixedTasks = fixedTasks
        )

        for (scheduled in scheduledResults) {
            if (scheduled.scheduledStart != null && (scheduled.scheduledEnd != null)) {
                database.taskDao().updateTaskSchedule(
                    id = scheduled.id,
                    start = scheduled.scheduledStart,
                    end = scheduled.scheduledEnd,
                    isAutoScheduled = scheduled.isAutoScheduled
                )
                if (!scheduled.isCompleted) {
                    NotificationScheduler.scheduleTaskNotification(context, scheduled)
                }
            } else {
                // Si no se pudo agendar, limpiar horario anterior si lo tenía
                database.taskDao().updateTaskSchedule(
                    id = scheduled.id,
                    start = null,
                    end = null,
                    isAutoScheduled = false
                )
                NotificationScheduler.cancelTaskNotification(context, scheduled.id)
            }
        }
    }
}
