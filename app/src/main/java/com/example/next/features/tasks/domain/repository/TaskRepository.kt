package com.example.next.features.tasks.domain.repository

import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(): Flow<Result<List<Task>>>
    fun getTaskById(id: String): Flow<Result<Task?>>
    suspend fun saveTask(task: Task): Result<Unit>
    suspend fun deleteTask(id: String): Result<Unit>
    suspend fun toggleTaskCompletion(id: String): Result<Unit>
    suspend fun incrementCompletedPomodoros(id: String): Result<Unit>
    suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit>
    fun searchTasks(query: String): Flow<Result<List<Task>>>
    suspend fun clearAllTasks(): Result<Unit>
    suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit>
}
