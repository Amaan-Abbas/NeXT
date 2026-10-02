package com.example.next.features.settings.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.home.domain.repository.HomeRepository
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import com.example.next.features.tasks.domain.repository.TaskRepository

class ClearAllAppDataUseCase(
    private val taskRepository: TaskRepository,
    private val pomodoroRepository: PomodoroRepository,
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            taskRepository.clearAllTasks()
            pomodoroRepository.clearAllSessions()
            homeRepository.clearAll()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to clear application data")
        }
    }
}
