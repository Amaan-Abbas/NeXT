package com.example.next.features.pomodoro.domain.model

enum class PomodoroMode(val defaultDurationSeconds: Int, val title: String) {
    FOCUS(25 * 60, "Focus"),
    SHORT_BREAK(5 * 60, "Short Break"),
    LONG_BREAK(15 * 60, "Long Break")
}
