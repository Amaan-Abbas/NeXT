package com.example.next.features.pomodoro.domain.usecase

import com.example.next.features.pomodoro.domain.engine.PomodoroTimerEngine

class ResetPomodoroUseCase(
    private val timerEngine: PomodoroTimerEngine
) {
    operator fun invoke() {
        timerEngine.reset()
    }
}
