package com.example.next.features.settings.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import com.example.next.features.settings.domain.model.AutoDeleteUnit
import com.example.next.features.tasks.domain.repository.TaskRepository

class AutoDeleteOldDataUseCase(
    private val taskRepository: TaskRepository,
    private val pomodoroRepository: PomodoroRepository
) {
    suspend operator fun invoke(
        unit: AutoDeleteUnit,
        value: Int,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Result<Unit> {
        if (unit == AutoDeleteUnit.DISABLED || value <= 0) {
            return Result.Success(Unit)
        }

        val millisPerDay = 86_400_000L
        val durationMillis = when (unit) {
            AutoDeleteUnit.DISABLED -> 0L
            AutoDeleteUnit.DAYS -> value * millisPerDay
            AutoDeleteUnit.WEEKS -> value * 7 * millisPerDay
            AutoDeleteUnit.MONTHS -> value * 30 * millisPerDay
        }

        if (durationMillis <= 0L) {
            return Result.Success(Unit)
        }

        val cutoffTimestamp = currentTimeMillis - durationMillis

        return try {
            taskRepository.deleteTasksOlderThan(cutoffTimestamp)
            pomodoroRepository.deleteSessionsOlderThan(cutoffTimestamp)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to perform auto-deletion of old data")
        }
    }
}
