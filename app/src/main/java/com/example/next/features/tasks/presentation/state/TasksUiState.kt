package com.example.next.features.tasks.presentation.state

import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.features.tasks.domain.model.TaskSortOption

enum class TaskTab {
    ACTIVE,
    COMPLETED
}

data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val selectedTab: TaskTab = TaskTab.ACTIVE,
    val selectedPriorityFilter: TaskPriority? = null,
    val selectedSortOption: TaskSortOption = TaskSortOption.DUE_DATE,
    val searchQuery: String = "",
    val error: String? = null,
    val isAddEditDialogShowing: Boolean = false,
    val editingTask: Task? = null,
    val taskPendingDeletion: Task? = null,
    val recentlyDeletedTask: Task? = null,
    val userMessage: String? = null,
    val activePomodoroTaskId: String? = null,
    val isPomodoroActive: Boolean = false
)
