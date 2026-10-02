package com.example.next.features.tasks.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.features.tasks.domain.model.TaskSortOption
import com.example.next.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTasksUseCase(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(
        priorityFilter: TaskPriority? = null,
        searchQuery: String = "",
        sortOption: TaskSortOption = TaskSortOption.DUE_DATE
    ): Flow<Result<List<Task>>> {
        return taskRepository.getTasks().map { result ->
            when (result) {
                is Result.Success -> {
                    var list = result.data

                    if (priorityFilter != null) {
                        list = list.filter { it.priority == priorityFilter }
                    }

                    if (searchQuery.isNotBlank()) {
                        val trimmedQuery = searchQuery.trim()
                        list = list.filter {
                            it.title.contains(trimmedQuery, ignoreCase = true) ||
                                    it.description.contains(trimmedQuery, ignoreCase = true)
                        }
                    }

                    val sortedList = when (sortOption) {
                        TaskSortOption.DUE_DATE -> list.sortedWith(
                            compareBy<Task, Long?>(nullsLast()) { it.dueDate }
                                .thenByDescending { it.priority.ordinal }
                                .thenByDescending { it.createdAt }
                        )
                        TaskSortOption.PRIORITY -> list.sortedWith(
                            compareByDescending<Task> { it.priority.ordinal }
                                .thenBy(nullsLast()) { it.dueDate }
                                .thenByDescending { it.createdAt }
                        )
                        TaskSortOption.CREATED_DATE -> list.sortedByDescending { it.createdAt }
                        TaskSortOption.ALPHABETICAL -> list.sortedBy { it.title.lowercase() }
                    }

                    Result.Success(sortedList)
                }
                is Result.Error -> result
                is Result.Loading -> Result.Loading
            }
        }
    }
}
