package com.example.next.features.pomodoro.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPomodoroSessionsUseCaseTest {

    private val sampleSessions = listOf(
        PomodoroSession(id = "s1", taskId = "t1", sessionType = PomodoroMode.FOCUS, isCompleted = true),
        PomodoroSession(id = "s2", taskId = "t2", sessionType = PomodoroMode.SHORT_BREAK, isCompleted = true)
    )

    private val fakeRepository = object : PomodoroRepository {
        override fun getSessions(): Flow<Result<List<PomodoroSession>>> = flowOf(Result.Success(sampleSessions))
        override fun getSessionsForTask(taskId: String): Flow<Result<List<PomodoroSession>>> =
            flowOf(Result.Success(sampleSessions.filter { it.taskId == taskId }))
        override suspend fun saveSession(session: PomodoroSession): Result<Unit> = Result.Success(Unit)
        override suspend fun clearAllSessions(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    private val useCase = GetPomodoroSessionsUseCase(fakeRepository)

    @Test
    fun `invoke without taskId returns all sessions`() = runBlocking {
        val result = useCase().first()
        assertTrue(result is Result.Success)
        val sessions = (result as Result.Success).data
        assertEquals(2, sessions.size)
    }

    @Test
    fun `invoke with taskId returns sessions for specific task`() = runBlocking {
        val result = useCase(taskId = "t1").first()
        assertTrue(result is Result.Success)
        val sessions = (result as Result.Success).data
        assertEquals(1, sessions.size)
        assertEquals("s1", sessions.first().id)
    }
}
