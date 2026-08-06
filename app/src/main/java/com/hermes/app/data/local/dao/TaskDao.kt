package com.hermes.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hermes.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, scheduledStart ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE (scheduledStart <= :endOfDay AND (scheduledEnd >= :startOfDay OR scheduledEnd IS NULL)) 
           OR (scheduledStart IS NULL AND createdAt >= :startOfDay AND createdAt <= :endOfDay)
        ORDER BY isCompleted ASC, scheduledStart ASC, priority DESC
    """)
    fun getTasksForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isFixed = 1 AND isCompleted = 0 ORDER BY scheduledStart ASC")
    suspend fun getFixedTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isFixed = 0 AND isCompleted = 0 AND scheduledStart IS NULL ORDER BY priority DESC, deadline ASC")
    suspend fun getUnscheduledFlexibleTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isFixed = 0 AND isCompleted = 0 ORDER BY priority DESC, deadline ASC")
    suspend fun getAllFlexibleTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND scheduledStart IS NOT NULL")
    suspend fun getActiveScheduledTasks(): List<TaskEntity>

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, isCompleted: Boolean)

    @Query("UPDATE tasks SET scheduledStart = :start, scheduledEnd = :end, isAutoScheduled = :isAutoScheduled WHERE id = :id")
    suspend fun updateTaskSchedule(id: Long, start: Long?, end: Long?, isAutoScheduled: Boolean)

    @Query("UPDATE tasks SET preGeneratedMessage = :message WHERE id = :id")
    suspend fun updatePreGeneratedMessage(id: Long, message: String?)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)
}
