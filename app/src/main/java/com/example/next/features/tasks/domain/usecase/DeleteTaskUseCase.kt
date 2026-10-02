package com.example.next.features.tasks.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.repository.TaskRepository

class DeleteTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return taskRepository.deleteTask(id)
    }
}
