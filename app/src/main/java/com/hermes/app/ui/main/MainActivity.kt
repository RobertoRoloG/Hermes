package com.hermes.app.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hermes.app.data.local.HermesDatabase
import com.hermes.app.domain.RoleManager
import com.hermes.app.domain.scheduler.TaskSchedulerEngine
import com.hermes.app.ui.assistant.AssistantRoleScreen
import com.hermes.app.ui.day.MyDayScreen
import com.hermes.app.ui.stats.StatisticsScreen
import com.hermes.app.ui.theme.*
import com.hermes.app.ui.viewmodel.TaskViewModel
import com.hermes.app.utils.IntentParser
import com.hermes.app.utils.NotificationScheduler
import com.hermes.app.utils.ShortcutHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private lateinit var database: HermesDatabase
    private lateinit var roleManager: RoleManager
    private val schedulerEngine = TaskSchedulerEngine()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = HermesDatabase.getDatabase(this)
        roleManager = RoleManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        NotificationScheduler.createNotificationChannel(this)
        ShortcutHelper.updateAssistantDynamicShortcut(this, roleManager.getAssistantName())

        handleIncomingIntent(intent)

        setContent {
            val taskViewModel: TaskViewModel = viewModel()
            val tasks by taskViewModel.allTasks.collectAsState()
            val pagerState = rememberPagerState(pageCount = { 3 })
            val scope = rememberCoroutineScope()

            CyberpunkTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = SurfaceDark,
                            contentColor = TextPrimary
                        ) {
                            // 0: Mi Día
                            NavigationBarItem(
                                selected = pagerState.currentPage == 0,
                                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                                label = {
                                    Text(
                                        "Mi Día",
                                        fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Mi Día"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )

                            // 1: Estadísticas
                            NavigationBarItem(
                                selected = pagerState.currentPage == 1,
                                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                                label = {
                                    Text(
                                        "Stats",
                                        fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = "Estadísticas"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonAmber,
                                    indicatorColor = NeonAmber,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )

                            // 2: Asistente
                            NavigationBarItem(
                                selected = pagerState.currentPage == 2,
                                onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                                label = {
                                    Text(
                                        "Asistente",
                                        fontWeight = if (pagerState.currentPage == 2) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = "Asistente"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (page) {
                                0 -> MyDayScreen(
                                    viewModel = taskViewModel
                                )
                                1 -> StatisticsScreen(
                                    tasks = tasks,
                                    roleManager = roleManager
                                )
                                2 -> AssistantRoleScreen(
                                    roleManager = roleManager,
                                    onUpdateAssistantName = { newName ->
                                        roleManager.setAssistantName(newName)
                                        ShortcutHelper.updateAssistantDynamicShortcut(this@MainActivity, newName)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        val parsedTask = IntentParser.parseTaskFromIntent(intent) ?: return
        val taskWithRoleContext = roleManager.applyRoleContextToTask(parsedTask)

        lifecycleScope.launch(Dispatchers.IO) {
            val insertedId = database.taskDao().insertTask(taskWithRoleContext)
            val savedTask = taskWithRoleContext.copy(id = insertedId)

            if (!savedTask.isFixed) {
                runAutoScheduler()
            } else if (savedTask.scheduledStart != null) {
                NotificationScheduler.scheduleTaskNotification(this@MainActivity, savedTask)
            }

            val feedback = roleManager.generateDynamicNotificationMessage(savedTask)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, feedback, Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun runAutoScheduler() {
        val fixedTasks = database.taskDao().getFixedTasks()
        val unscheduledFlexible = database.taskDao().getUnscheduledFlexibleTasks()

        if (unscheduledFlexible.isEmpty()) return

        val scheduledResults = schedulerEngine.batchScheduleFlexibleTasks(
            flexibleTasks = unscheduledFlexible,
            startSearchDate = Calendar.getInstance(),
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
                NotificationScheduler.scheduleTaskNotification(this@MainActivity, scheduled)
            }
        }
    }
}
