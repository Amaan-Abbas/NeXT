package com.example.next.features.pomodoro.domain.model

data class PomodoroConfig(
    val focusDurationMinutes: Int = 25,
    val shortBreakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15,
    val sessionsPerLongBreak: Int = 4,
    val autoBumpTargetOnOverflow: Boolean = true,
    val allowSoftTargetOverflow: Boolean = true
) {
    fun getDurationSeconds(mode: PomodoroMode): Int = when (mode) {
        PomodoroMode.FOCUS -> focusDurationMinutes * 60
        PomodoroMode.SHORT_BREAK -> shortBreakDurationMinutes * 60
        PomodoroMode.LONG_BREAK -> longBreakDurationMinutes * 60
    }
}
