package com.example.next.features.pomodoro.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow

class GetPomodoroHistoryUseCase(
    private val repository: PomodoroRepository
) {
    operator fun invoke(): Flow<Result<List<PomodoroSession>>> {
        return repository.getSessions()
    }
}
