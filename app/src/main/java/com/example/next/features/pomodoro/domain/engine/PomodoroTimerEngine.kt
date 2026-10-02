package com.example.next.features.pomodoro.domain.engine

import com.example.next.features.pomodoro.domain.model.PomodoroConfig
import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.model.TimerState
import com.example.next.features.pomodoro.domain.usecase.SavePomodoroSessionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class PomodoroEngineState(
    val timerState: TimerState = TimerState.IDLE,
    val mode: PomodoroMode = PomodoroMode.FOCUS,
    val timeRemainingSeconds: Int = PomodoroConfig().getDurationSeconds(PomodoroMode.FOCUS),
    val totalDurationSeconds: Int = PomodoroConfig().getDurationSeconds(PomodoroMode.FOCUS),
    val selectedTaskId: String? = null,
    val completedFocusCount: Int = 0,
    val currentSessionId: String? = null,
    val sessionStartTime: Long = 0L,
    val config: PomodoroConfig = PomodoroConfig()
)

class PomodoroTimerEngine(
    private val savePomodoroSessionUseCase: SavePomodoroSessionUseCase,
    private var config: PomodoroConfig = PomodoroConfig(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    private val _engineState = MutableStateFlow(
        PomodoroEngineState(
            timeRemainingSeconds = config.getDurationSeconds(PomodoroMode.FOCUS),
            totalDurationSeconds = config.getDurationSeconds(PomodoroMode.FOCUS),
            config = config
        )
    )
    val engineState: StateFlow<PomodoroEngineState> = _engineState.asStateFlow()

    private var tickerJob: Job? = null
    private var targetEndTimeMillis: Long = 0L

    fun updateConfig(newConfig: PomodoroConfig) {
        config = newConfig
        if (_engineState.value.timerState == TimerState.IDLE) {
            val duration = config.getDurationSeconds(_engineState.value.mode)
            _engineState.update {
                it.copy(
                    config = config,
                    timeRemainingSeconds = duration,
                    totalDurationSeconds = duration
                )
            }
        } else {
            _engineState.update { it.copy(config = config) }
        }
    }

    fun start() {
        val currentState = _engineState.value
        if (currentState.timerState == TimerState.RUNNING) return

        val duration = if (currentState.timeRemainingSeconds <= 0) {
            config.getDurationSeconds(currentState.mode)
        } else {
            currentState.timeRemainingSeconds
        }

        val totalDuration = if (currentState.timeRemainingSeconds <= 0) {
            duration
        } else {
            currentState.totalDurationSeconds
        }

        val now = System.currentTimeMillis()
        val sessionId = currentState.currentSessionId ?: UUID.randomUUID().toString()
        val startTime = if (currentState.sessionStartTime == 0L) now else currentState.sessionStartTime

        targetEndTimeMillis = now + (duration * 1000L)

        _engineState.update {
            it.copy(
                timerState = TimerState.RUNNING,
                timeRemainingSeconds = duration,
                totalDurationSeconds = totalDuration,
                currentSessionId = sessionId,
                sessionStartTime = startTime
            )
        }

        startTicker()
    }

    fun pause() {
        tickerJob?.cancel()
        val now = System.currentTimeMillis()
        val remaining = maxOf(0, ((targetEndTimeMillis - now + 999) / 1000).toInt())
        _engineState.update {
            it.copy(
                timerState = TimerState.PAUSED,
                timeRemainingSeconds = remaining
            )
        }
    }

    fun resume() {
        start()
    }

    fun reset() {
        tickerJob?.cancel()
        val currentState = _engineState.value
        if (currentState.timerState == TimerState.RUNNING || currentState.timerState == TimerState.PAUSED) {
            recordSession(isCompleted = false, isInterrupted = true)
        }

        val duration = config.getDurationSeconds(currentState.mode)
        _engineState.update {
            it.copy(
                timerState = TimerState.IDLE,
                timeRemainingSeconds = duration,
                totalDurationSeconds = duration,
                currentSessionId = null,
                sessionStartTime = 0L
            )
        }
    }

    fun skip() {
        tickerJob?.cancel()
        val currentState = _engineState.value
        if (currentState.timerState == TimerState.RUNNING || currentState.timerState == TimerState.PAUSED) {
            recordSession(isCompleted = false, isInterrupted = true)
        }

        advanceToNextMode()
    }

    fun selectMode(mode: PomodoroMode) {
        tickerJob?.cancel()
        val currentState = _engineState.value
        if (currentState.timerState == TimerState.RUNNING || currentState.timerState == TimerState.PAUSED) {
            recordSession(isCompleted = false, isInterrupted = true)
        }

        val duration = config.getDurationSeconds(mode)
        _engineState.update {
            it.copy(
                mode = mode,
                timerState = TimerState.IDLE,
                timeRemainingSeconds = duration,
                totalDurationSeconds = duration,
                currentSessionId = null,
                sessionStartTime = 0L
            )
        }
    }

    fun selectTask(taskId: String?) {
        _engineState.update { it.copy(selectedTaskId = taskId) }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (_engineState.value.timerState == TimerState.RUNNING) {
                val now = System.currentTimeMillis()
                val remaining = maxOf(0, ((targetEndTimeMillis - now + 999) / 1000).toInt())

                if (remaining <= 0) {
                    _engineState.update {
                        it.copy(
                            timeRemainingSeconds = 0,
                            timerState = TimerState.COMPLETED
                        )
                    }
                    onTimerFinished()
                    break
                } else {
                    _engineState.update { it.copy(timeRemainingSeconds = remaining) }
                    delay(200L)
                }
            }
        }
    }

    private fun onTimerFinished() {
        val currentState = _engineState.value
        val isFocus = currentState.mode == PomodoroMode.FOCUS

        recordSession(isCompleted = true, isInterrupted = false)

        val newCompletedCount = if (isFocus) currentState.completedFocusCount + 1 else currentState.completedFocusCount
        _engineState.update { it.copy(completedFocusCount = newCompletedCount) }

        advanceToNextMode(newCompletedCount)
    }

    private fun advanceToNextMode(completedFocus: Int = _engineState.value.completedFocusCount) {
        val currentState = _engineState.value
        val nextMode = when (currentState.mode) {
            PomodoroMode.FOCUS -> {
                if (completedFocus > 0 && completedFocus % config.sessionsPerLongBreak == 0) {
                    PomodoroMode.LONG_BREAK
                } else {
                    PomodoroMode.SHORT_BREAK
                }
            }
            PomodoroMode.SHORT_BREAK -> PomodoroMode.FOCUS
            PomodoroMode.LONG_BREAK -> PomodoroMode.FOCUS
        }

        val duration = config.getDurationSeconds(nextMode)
        _engineState.update {
            it.copy(
                mode = nextMode,
                timerState = TimerState.IDLE,
                timeRemainingSeconds = duration,
                totalDurationSeconds = duration,
                currentSessionId = null,
                sessionStartTime = 0L
            )
        }
    }

    private fun recordSession(isCompleted: Boolean, isInterrupted: Boolean) {
        val state = _engineState.value
        val now = System.currentTimeMillis()
        val planned = state.totalDurationSeconds
        val actual = if (isCompleted) planned else maxOf(0, planned - state.timeRemainingSeconds)

        val session = PomodoroSession(
            id = state.currentSessionId ?: UUID.randomUUID().toString(),
            taskId = state.selectedTaskId,
            sessionType = state.mode,
            startedAt = if (state.sessionStartTime != 0L) state.sessionStartTime else now,
            endedAt = now,
            plannedDurationSeconds = planned,
            actualDurationSeconds = actual,
            isCompleted = isCompleted,
            isInterrupted = isInterrupted
        )

        scope.launch {
            savePomodoroSessionUseCase(session)
        }
    }
}
