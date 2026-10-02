package com.example.next.features.pomodoro.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow

class GetPomodoroSessionsUseCase(
    private val pomodoroRepository: PomodoroRepository
) {
    operator fun invoke(taskId: String? = null): Flow<Result<List<PomodoroSession>>> {
        return if (taskId != null) {
            pomodoroRepository.getSessionsForTask(taskId)
        } else {
            pomodoroRepository.getSessions()
        }
    }
}
