package com.example.next.features.pomodoro.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.pomodoro.domain.model.PomodoroConfig
import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavePomodoroSessionUseCaseTest {

    private val savedSessions = mutableListOf<PomodoroSession>()
    private var incrementedTaskId: String? = null
    private var updatedEstimatedTaskId: String? = null
    private var updatedEstimatedValue: Int? = null

    private val sampleTask = Task(
        id = "t1",
        title = "Test Task",
        estimatedPomodoros = 2,
        completedPomodoros = 2
    )

    private val fakePomodoroRepository = object : PomodoroRepository {
        override fun getSessions(): Flow<Result<List<PomodoroSession>>> = flowOf(Result.Success(savedSessions))
        override fun getSessionsForTask(taskId: String): Flow<Result<List<PomodoroSession>>> =
            flowOf(Result.Success(savedSessions.filter { it.taskId == taskId }))
        override suspend fun saveSession(session: PomodoroSession): Result<Unit> {
            savedSessions.add(session)
            return Result.Success(Unit)
        }
        override suspend fun clearAllSessions(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    private val fakeTaskRepository = object : TaskRepository {
        override fun getTasks(): Flow<Result<List<Task>>> = flowOf(Result.Success(listOf(sampleTask)))
        override fun getTaskById(id: String): Flow<Result<Task?>> = flowOf(Result.Success(if (id == "t1") sampleTask else null))
        override suspend fun saveTask(task: Task): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTask(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun toggleTaskCompletion(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> {
            incrementedTaskId = id
            return Result.Success(Unit)
        }
        override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> {
            updatedEstimatedTaskId = id
            updatedEstimatedValue = newEstimated
            return Result.Success(Unit)
        }
        override fun searchTasks(query: String): Flow<Result<List<Task>>> = flowOf(Result.Success(emptyList()))
        override suspend fun clearAllTasks(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    @Test
    fun `saving completed focus session increments completed task pomodoros and auto-bumps estimated target when overflow occurs`() = runBlocking {
        val useCase = SavePomodoroSessionUseCase(
            pomodoroRepository = fakePomodoroRepository,
            taskRepository = fakeTaskRepository,
            config = PomodoroConfig(autoBumpTargetOnOverflow = true)
        )

        val session = PomodoroSession(
            id = "s1",
            taskId = "t1",
            sessionType = PomodoroMode.FOCUS,
            isCompleted = true
        )

        val result = useCase(session)

        assertTrue(result is Result.Success)
        assertEquals(1, savedSessions.size)
        assertEquals("t1", incrementedTaskId)
        assertEquals("t1", updatedEstimatedTaskId)
        assertEquals(3, updatedEstimatedValue) // auto-bumped from 2 to 3
    }

    @Test
    fun `saving interrupted focus session does not increment task completed pomodoros`() = runBlocking {
        incrementedTaskId = null
        savedSessions.clear()

        val useCase = SavePomodoroSessionUseCase(
            pomodoroRepository = fakePomodoroRepository,
            taskRepository = fakeTaskRepository,
            config = PomodoroConfig()
        )

        val session = PomodoroSession(
            id = "s2",
            taskId = "t1",
            sessionType = PomodoroMode.FOCUS,
            isCompleted = false,
            isInterrupted = true
        )

        val result = useCase(session)

        assertTrue(result is Result.Success)
        assertEquals(1, savedSessions.size)
        assertEquals(null, incrementedTaskId)
    }

    @Test
    fun `saving standalone completed focus session does not fail or interact with task repository`() = runBlocking {
        incrementedTaskId = null
        savedSessions.clear()

        val useCase = SavePomodoroSessionUseCase(
            pomodoroRepository = fakePomodoroRepository,
            taskRepository = fakeTaskRepository,
            config = PomodoroConfig()
        )

        val session = PomodoroSession(
            id = "s3",
            taskId = null,
            sessionType = PomodoroMode.FOCUS,
            isCompleted = true,
            isInterrupted = false
        )

        val result = useCase(session)

        assertTrue(result is Result.Success)
        assertEquals(1, savedSessions.size)
        assertEquals(null, incrementedTaskId)
    }
}

