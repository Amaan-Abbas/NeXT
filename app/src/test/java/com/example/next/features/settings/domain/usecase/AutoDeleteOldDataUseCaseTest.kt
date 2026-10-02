package com.example.next.features.settings.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import com.example.next.features.settings.domain.model.AutoDeleteUnit
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoDeleteOldDataUseCaseTest {

    private var tasksCutoff: Long? = null
    private var pomodoroCutoff: Long? = null

    private val fakeTaskRepository = object : TaskRepository {
        override fun getTasks(): Flow<Result<List<Task>>> = MutableStateFlow(Result.Success(emptyList()))
        override fun getTaskById(id: String): Flow<Result<Task?>> = MutableStateFlow(Result.Success(null))
        override suspend fun saveTask(task: Task): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTask(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun toggleTaskCompletion(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> = Result.Success(Unit)
        override fun searchTasks(query: String): Flow<Result<List<Task>>> = MutableStateFlow(Result.Success(emptyList()))
        override suspend fun clearAllTasks(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> {
            tasksCutoff = cutoffTimestamp
            return Result.Success(Unit)
        }
    }

    private val fakePomodoroRepository = object : PomodoroRepository {
        override fun getSessions(): Flow<Result<List<PomodoroSession>>> = MutableStateFlow(Result.Success(emptyList()))
        override fun getSessionsForTask(taskId: String): Flow<Result<List<PomodoroSession>>> = MutableStateFlow(Result.Success(emptyList()))
        override suspend fun saveSession(session: PomodoroSession): Result<Unit> = Result.Success(Unit)
        override suspend fun clearAllSessions(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long): Result<Unit> {
            pomodoroCutoff = cutoffTimestamp
            return Result.Success(Unit)
        }
    }

    private val useCase = AutoDeleteOldDataUseCase(
        taskRepository = fakeTaskRepository,
        pomodoroRepository = fakePomodoroRepository
    )

    @Test
    fun `invoke when unit is DISABLED does not delete data`() = runBlocking {
        val result = useCase(AutoDeleteUnit.DISABLED, 30)

        assertTrue(result is Result.Success)
        assertNull(tasksCutoff)
        assertNull(pomodoroCutoff)
    }

    @Test
    fun `invoke with 7 DAYS calculates correct cutoff`() = runBlocking {
        val now = 1_000_000_000_000L
        val expectedCutoff = now - (7 * 86_400_000L)

        val result = useCase(AutoDeleteUnit.DAYS, 7, currentTimeMillis = now)

        assertTrue(result is Result.Success)
        assertEquals(expectedCutoff, tasksCutoff)
        assertEquals(expectedCutoff, pomodoroCutoff)
    }

    @Test
    fun `invoke with 2 WEEKS calculates correct cutoff`() = runBlocking {
        val now = 1_000_000_000_000L
        val expectedCutoff = now - (14 * 86_400_000L)

        val result = useCase(AutoDeleteUnit.WEEKS, 2, currentTimeMillis = now)

        assertTrue(result is Result.Success)
        assertEquals(expectedCutoff, tasksCutoff)
        assertEquals(expectedCutoff, pomodoroCutoff)
    }
}
