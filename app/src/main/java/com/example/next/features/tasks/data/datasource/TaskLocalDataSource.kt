package com.example.next.features.tasks.data.datasource

import com.example.next.data.local.dao.TaskDao
import com.example.next.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskLocalDataSource(
    private val taskDao: TaskDao
) {
    fun getTasksFlow(): Flow<List<TaskEntity>> {
        return taskDao.getTasks()
    }

    fun searchTasksFlow(query: String): Flow<List<TaskEntity>> {
        return taskDao.searchTasks(query)
    }

    fun getTaskByIdFlow(id: String): Flow<TaskEntity?> {
        return taskDao.getTaskById(id)
    }

    suspend fun getTaskByIdOneShot(id: String): TaskEntity? {
        return taskDao.getTaskByIdOneShot(id)
    }

    suspend fun saveTask(taskEntity: TaskEntity) {
        val finalEntity = if (taskEntity.id.isBlank()) {
            taskEntity.copy(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        } else {
            taskEntity.copy(updatedAt = System.currentTimeMillis())
        }
        taskDao.insertTask(finalEntity)
    }

    suspend fun deleteTask(id: String) {
        taskDao.deleteTask(id)
    }

    suspend fun toggleTaskCompletion(id: String) {
        val existingTask = taskDao.getTaskByIdOneShot(id)
        if (existingTask != null) {
            val newCompletedState = !existingTask.isCompleted
            val now = System.currentTimeMillis()
            val completedAt = if (newCompletedState) now else null
            val createdAt = if (!newCompletedState) now else existingTask.createdAt
            taskDao.updateTaskCompletionState(
                id = id,
                isCompleted = newCompletedState,
                completedAt = completedAt,
                updatedAt = now,
                createdAt = createdAt
            )
        }
    }

    suspend fun incrementCompletedPomodoros(id: String) {
        taskDao.incrementCompletedPomodoros(id, System.currentTimeMillis())
    }

    suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?) {
        taskDao.updateEstimatedPomodoros(id, newEstimated, System.currentTimeMillis())
    }

    suspend fun clearAllTasks() {
        taskDao.clearAllTasks()
    }

    suspend fun deleteTasksOlderThan(cutoffTimestamp: Long) {
        taskDao.deleteTasksOlderThan(cutoffTimestamp)
    }
}
