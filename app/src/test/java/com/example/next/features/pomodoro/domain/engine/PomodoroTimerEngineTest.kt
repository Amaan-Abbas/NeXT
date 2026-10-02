package com.example.next.features.pomodoro.domain.engine

import com.example.next.core.common.Result
import com.example.next.features.pomodoro.domain.model.PomodoroConfig
import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.model.TimerState
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import com.example.next.features.pomodoro.domain.usecase.SavePomodoroSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PomodoroTimerEngineTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val fakePomodoroRepository = object : PomodoroRepository {
        override fun getSessions(): Flow<Result<List<PomodoroSession>>> = flowOf(Result.Success(emptyList()))
        override fun getSessionsForTask(taskId: String): Flow<Result<List<PomodoroSession>>> = flowOf(Result.Success(emptyList()))
        override suspend fun saveSession(session: PomodoroSession): Result<Unit> = Result.Success(Unit)
        override suspend fun clearAllSessions(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    private val savePomodoroSessionUseCase = SavePomodoroSessionUseCase(fakePomodoroRepository)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state starts at IDLE with FOCUS mode default duration`() {
        val config = PomodoroConfig(focusDurationMinutes = 25)
        val engine = PomodoroTimerEngine(
            savePomodoroSessionUseCase = savePomodoroSessionUseCase,
            config = config,
            scope = testScope
        )

        val state = engine.engineState.value
        assertEquals(TimerState.IDLE, state.timerState)
        assertEquals(PomodoroMode.FOCUS, state.mode)
        assertEquals(1500, state.timeRemainingSeconds)
    }

    @Test
    fun `start updates state to RUNNING`() {
        val config = PomodoroConfig(focusDurationMinutes = 25)
        val engine = PomodoroTimerEngine(
            savePomodoroSessionUseCase = savePomodoroSessionUseCase,
            config = config,
            scope = testScope
        )

        engine.start()

        val state = engine.engineState.value
        assertEquals(TimerState.RUNNING, state.timerState)
    }

    @Test
    fun `pause updates state to PAUSED`() {
        val config = PomodoroConfig(focusDurationMinutes = 25)
        val engine = PomodoroTimerEngine(
            savePomodoroSessionUseCase = savePomodoroSessionUseCase,
            config = config,
            scope = testScope
        )

        engine.start()
        engine.pause()

        val state = engine.engineState.value
        assertEquals(TimerState.PAUSED, state.timerState)
    }

    @Test
    fun `reset returns state to IDLE and mode default duration`() {
        val config = PomodoroConfig(focusDurationMinutes = 25)
        val engine = PomodoroTimerEngine(
            savePomodoroSessionUseCase = savePomodoroSessionUseCase,
            config = config,
            scope = testScope
        )

        engine.start()
        engine.reset()

        val state = engine.engineState.value
        assertEquals(TimerState.IDLE, state.timerState)
        assertEquals(1500, state.timeRemainingSeconds)
    }

    @Test
    fun `skip transitions from FOCUS to SHORT_BREAK`() {
        val config = PomodoroConfig(focusDurationMinutes = 25, shortBreakDurationMinutes = 5)
        val engine = PomodoroTimerEngine(
            savePomodoroSessionUseCase = savePomodoroSessionUseCase,
            config = config,
            scope = testScope
        )

        engine.skip()

        val state = engine.engineState.value
        assertEquals(TimerState.IDLE, state.timerState)
        assertEquals(PomodoroMode.SHORT_BREAK, state.mode)
        assertEquals(300, state.timeRemainingSeconds)
    }
}
