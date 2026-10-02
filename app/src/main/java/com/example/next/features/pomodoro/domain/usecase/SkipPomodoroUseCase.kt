package com.example.next.features.pomodoro.domain.usecase

import com.example.next.features.pomodoro.domain.engine.PomodoroTimerEngine

class SkipPomodoroUseCase(
    private val timerEngine: PomodoroTimerEngine
) {
    operator fun invoke() {
        timerEngine.skip()
    }
}
