package com.example.next.features.pomodoro.presentation.state

import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.pomodoro.domain.model.TimerState
import com.example.next.features.tasks.domain.model.Task

data class PomodoroUiState(
    val mode: PomodoroMode = PomodoroMode.FOCUS,
    val timerState: TimerState = TimerState.IDLE,
    val timeRemainingSeconds: Int = PomodoroMode.FOCUS.defaultDurationSeconds,
    val totalDurationSeconds: Int = PomodoroMode.FOCUS.defaultDurationSeconds,
    val selectedTask: Task? = null,
    val availableTasks: List<Task> = emptyList(),
    val completedSessionsCount: Int = 0,
    val sessionsPerLongBreak: Int = 4,
    val isTaskSelectorOpen: Boolean = false,
    val taskSearchQuery: String = "",
    val completionEventMessage: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
