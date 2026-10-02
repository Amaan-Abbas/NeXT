package com.example.next.features.tasks.data.repository

import com.example.next.core.common.DispatcherProvider
import com.example.next.core.common.Result
import com.example.next.core.common.asResult
import com.example.next.data.mapper.toDomain
import com.example.next.data.mapper.toDomainList
import com.example.next.data.mapper.toEntity
import com.example.next.features.tasks.data.datasource.TaskLocalDataSource
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TaskRepositoryImpl(
    private val localDataSource: TaskLocalDataSource,
    private val dispatchers: DispatcherProvider
) : TaskRepository {

    override fun getTasks(): Flow<Result<List<Task>>> {
        return localDataSource.getTasksFlow()
            .map { entityList -> entityList.toDomainList() }
            .asResult()
            .flowOn(dispatchers.io)
    }

    override fun getTaskById(id: String): Flow<Result<Task?>> {
        return localDataSource.getTaskByIdFlow(id)
            .map { entity -> entity?.toDomain() }
            .asResult()
            .flowOn(dispatchers.io)
    }

    override fun searchTasks(query: String): Flow<Result<List<Task>>> {
        return localDataSource.searchTasksFlow(query)
            .map { entityList -> entityList.toDomainList() }
            .asResult()
            .flowOn(dispatchers.io)
    }

    override suspend fun saveTask(task: Task): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.saveTask(task.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to save task to database")
        }
    }

    override suspend fun deleteTask(id: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.deleteTask(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to delete task from database")
        }
    }

    override suspend fun toggleTaskCompletion(id: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.toggleTaskCompletion(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to toggle task completion state")
        }
    }

    override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.incrementCompletedPomodoros(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to increment completed pomodoros")
        }
    }

    override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.updateEstimatedPomodoros(id, newEstimated)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to update estimated pomodoros")
        }
    }

    override suspend fun clearAllTasks(): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.clearAllTasks()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to clear tasks from database")
        }
    }

    override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.deleteTasksOlderThan(cutoffTimestamp)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to delete old tasks from database")
        }
    }
}
