package com.example.next.features.tasks.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class SearchTasksUseCase(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(query: String): Flow<Result<List<Task>>> {
        return taskRepository.searchTasks(query.trim())
    }
}
