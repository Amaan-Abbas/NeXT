package com.example.next.features.tasks.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.repository.TaskRepository

class SaveTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        if (task.title.isBlank()) {
            return Result.Error(message = "Task title cannot be empty")
        }
        return taskRepository.saveTask(task)
    }
}
