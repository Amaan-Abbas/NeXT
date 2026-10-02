package com.example.next.features.tasks.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.repository.TaskRepository

class CreateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        val trimmedTitle = task.title.trim()
        if (trimmedTitle.isBlank()) {
            return Result.Error(message = "Task title cannot be empty")
        }
        val validatedTask = task.copy(
            title = trimmedTitle,
            description = task.description.trim(),
            estimatedPomodoros = if (task.estimatedPomodoros != null && task.estimatedPomodoros < 1) 1 else task.estimatedPomodoros
        )
        return taskRepository.saveTask(validatedTask)
    }
}
