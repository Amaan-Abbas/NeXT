package com.example.next.features.settings.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.home.domain.model.HomeItem
import com.example.next.features.home.domain.repository.HomeRepository
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class ClearAllAppDataUseCaseTest {

    private var tasksCleared = false
    private var pomodoroCleared = false
    private var homeCleared = false

    private val fakeTaskRepository = object : TaskRepository {
        override fun getTasks(): Flow<Result<List<Task>>> = MutableStateFlow(Result.Success(emptyList()))
        override fun getTaskById(id: String): Flow<Result<Task?>> = MutableStateFlow(Result.Success(null))
        override suspend fun saveTask(task: Task): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTask(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun toggleTaskCompletion(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> = Result.Success(Unit)
        override fun searchTasks(query: String): Flow<Result<List<Task>>> = MutableStateFlow(Result.Success(emptyList()))
        override suspend fun clearAllTasks(): Result<Unit> {
            tasksCleared = true
            return Result.Success(Unit)
        }
        override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    private val fakePomodoroRepository = object : PomodoroRepository {
        override fun getSessions(): Flow<Result<List<PomodoroSession>>> = MutableStateFlow(Result.Success(emptyList()))
        override fun getSessionsForTask(taskId: String): Flow<Result<List<PomodoroSession>>> = MutableStateFlow(Result.Success(emptyList()))
        override suspend fun saveSession(session: PomodoroSession): Result<Unit> = Result.Success(Unit)
        override suspend fun clearAllSessions(): Result<Unit> {
            pomodoroCleared = true
            return Result.Success(Unit)
        }
        override suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    private val fakeHomeRepository = object : HomeRepository {
        override fun getHomeItems(): Flow<Result<List<HomeItem>>> = MutableStateFlow(Result.Success(emptyList()))
        override fun getItemById(id: String): Flow<Result<HomeItem>> = MutableStateFlow(Result.Error(message = "Not found"))
        override suspend fun toggleFavorite(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun refreshItems(): Result<Unit> = Result.Success(Unit)
        override suspend fun clearAll(): Result<Unit> {
            homeCleared = true
            return Result.Success(Unit)
        }
    }

    private val useCase = ClearAllAppDataUseCase(
        taskRepository = fakeTaskRepository,
        pomodoroRepository = fakePomodoroRepository,
        homeRepository = fakeHomeRepository
    )

    @Test
    fun `invoke clears all repositories successfully`() = runBlocking {
        val result = useCase()

        assertTrue(result is Result.Success)
        assertTrue(tasksCleared)
        assertTrue(pomodoroCleared)
        assertTrue(homeCleared)
    }
}
