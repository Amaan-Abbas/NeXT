package com.example.next.features.tasks.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateTaskUseCaseTest {

    private var lastSavedTask: Task? = null

    private val fakeRepository = object : TaskRepository {
        override fun getTasks(): Flow<Result<List<Task>>> = flowOf(Result.Success(emptyList()))
        override fun getTaskById(id: String): Flow<Result<Task?>> = flowOf(Result.Success(null))
        override suspend fun saveTask(task: Task): Result<Unit> {
            lastSavedTask = task
            return Result.Success(Unit)
        }
        override suspend fun deleteTask(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun toggleTaskCompletion(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> = Result.Success(Unit)
        override fun searchTasks(query: String): Flow<Result<List<Task>>> = flowOf(Result.Success(emptyList()))
        override suspend fun clearAllTasks(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    private val useCase = CreateTaskUseCase(fakeRepository)

    @Test
    fun `invoke with empty title returns Error`() = runBlocking {
        val task = Task(id = "", title = "   ", description = "Test")
        val result = useCase(task)
        assertTrue(result is Result.Error)
        assertEquals("Task title cannot be empty", (result as Result.Error).message)
    }

    @Test
    fun `invoke with valid title saves sanitized task`() = runBlocking {
        val task = Task(id = "", title = "  Clean Architecture  ", description = " Desc ", estimatedPomodoros = 0)
        val result = useCase(task)
        assertTrue(result is Result.Success)
        assertEquals("Clean Architecture", lastSavedTask?.title)
        assertEquals("Desc", lastSavedTask?.description)
        assertEquals(1, lastSavedTask?.estimatedPomodoros)
    }
}
