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

    fun addTask(task: TaskEntity) {
        val contextualized = roleManager.applyRoleContextToTask(task)
        viewModelScope.launch(Dispatchers.IO) {
            val insertedId = database.taskDao().insertTask(contextualized)
            var savedTask = contextualized.copy(id = insertedId)

            if (!savedTask.isFixed) {
                runAutoSchedulerForDate(_selectedCalendar.value)
            } else if (savedTask.scheduledStart != null) {
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
                    var startMs: Long? = null
                    var endMs: Long? = null

                    if (baseTask.isFixed) {
                        startMs = currentCal.timeInMillis
                        endMs = startMs + (baseTask.durationMinutes * 60 * 1000L)
                    }

                    val taskForDay = baseTask.copy(
                        id = 0,
                        scheduledStart = startMs,
                        scheduledEnd = endMs,
                        createdAt = System.currentTimeMillis()
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

            runAutoSchedulerForDate(startCalendar)

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
            runAutoSchedulerForDate(_selectedCalendar.value)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.taskDao().deleteTask(task)
            NotificationScheduler.cancelTaskNotification(context, task.id)
            runAutoSchedulerForDate(_selectedCalendar.value)
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
            runAutoSchedulerForDate(_selectedCalendar.value)
        }
    }

    fun runAutoScheduler() {
        viewModelScope.launch(Dispatchers.IO) {
            runAutoSchedulerForDate(_selectedCalendar.value)
        }
    }

    private suspend fun runAutoSchedulerForDate(searchDate: Calendar) {
        val fixedTasks = database.taskDao().getFixedTasks()
        val flexibleTasks = database.taskDao().getAllFlexibleTasks()

        if (flexibleTasks.isEmpty()) return

        val scheduledResults = schedulerEngine.batchScheduleFlexibleTasks(
            flexibleTasks = flexibleTasks,
            startSearchDate = searchDate,
            maxSearchDays = 7,
            fixedTasks = fixedTasks
        )

        for (scheduled in scheduledResults) {
            if (scheduled.scheduledStart != null && scheduled.scheduledEnd != null) {
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
                database.taskDao().updateTaskSchedule(scheduled.id, null, null, false)
                NotificationScheduler.cancelTaskNotification(context, scheduled.id)
            }
        }
    }
}
