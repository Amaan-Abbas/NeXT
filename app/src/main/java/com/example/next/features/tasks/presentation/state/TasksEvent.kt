package com.example.next.features.tasks.presentation.state

import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.features.tasks.domain.model.TaskSortOption

sealed interface TasksEvent {
    data object LoadTasks : TasksEvent
    data class SelectTab(val tab: TaskTab) : TasksEvent
    data class FilterByPriority(val priority: TaskPriority?) : TasksEvent
    data class SelectSortOption(val sortOption: TaskSortOption) : TasksEvent
    data class UpdateSearchQuery(val query: String) : TasksEvent
    data class ToggleTaskCompletion(val taskId: String) : TasksEvent
    data class DeleteTask(val taskId: String) : TasksEvent
    data object ConfirmDeleteTask : TasksEvent
    data object CancelDeleteTask : TasksEvent
    data object UndoDeleteTask : TasksEvent
    data class SaveTask(val task: Task) : TasksEvent
    data object ShowAddDialog : TasksEvent
    data class ShowEditDialog(val task: Task) : TasksEvent
    data object DismissDialog : TasksEvent
    data object ClearUserMessage : TasksEvent
}
