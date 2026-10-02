package com.example.next.features.pomodoro.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.pomodoro.domain.model.PomodoroConfig
import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import com.example.next.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.firstOrNull

class SavePomodoroSessionUseCase(
    private val pomodoroRepository: PomodoroRepository,
    private val taskRepository: TaskRepository? = null,
    private val config: PomodoroConfig = PomodoroConfig()
) {
    suspend operator fun invoke(session: PomodoroSession): Result<Unit> {
        val saveResult = pomodoroRepository.saveSession(session)
        if (saveResult is Result.Success && session.isCompleted && session.sessionType == PomodoroMode.FOCUS && session.taskId != null && taskRepository != null) {
            val taskResult = taskRepository.getTaskById(session.taskId).firstOrNull { it !is Result.Loading }
            if (taskResult is Result.Success && taskResult.data != null) {
                val currentTask = taskResult.data
                val newCompletedCount = currentTask.completedPomodoros + 1
                
                taskRepository.incrementCompletedPomodoros(session.taskId)
                
                // Auto-bump estimated pomodoros target if target exceeded and autoBump is enabled
                if (config.autoBumpTargetOnOverflow && currentTask.estimatedPomodoros != null && newCompletedCount > currentTask.estimatedPomodoros) {
                    taskRepository.updateEstimatedPomodoros(session.taskId, newCompletedCount)
                }
            } else {
                taskRepository.incrementCompletedPomodoros(session.taskId)
            }
        }
        return saveResult
    }
}
