package com.example.next.features.pomodoro.domain.model

data class PomodoroSession(
    val id: String,
    val taskId: String? = null,
    val sessionType: PomodoroMode = PomodoroMode.FOCUS,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val plannedDurationSeconds: Int = PomodoroMode.FOCUS.defaultDurationSeconds,
    val actualDurationSeconds: Int = 0,
    val isCompleted: Boolean = false,
    val isInterrupted: Boolean = false
)
