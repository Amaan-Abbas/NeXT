package com.example.next.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.next.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun getTasksOneShot(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskByIdOneShot(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String): Int

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt, updatedAt = :updatedAt, createdAt = :createdAt WHERE id = :id")
    suspend fun updateTaskCompletionState(id: String, isCompleted: Boolean, completedAt: Long?, updatedAt: Long, createdAt: Long): Int

    @Query("UPDATE tasks SET completedPomodoroSessions = completedPomodoroSessions + 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun incrementCompletedPomodoros(id: String, updatedAt: Long): Int

    @Query("UPDATE tasks SET estimatedPomodoroSessions = :newEstimated, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?, updatedAt: Long): Int

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks(): Int

    @Query("DELETE FROM tasks WHERE createdAt < :cutoffTimestamp")
    suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Int
}
