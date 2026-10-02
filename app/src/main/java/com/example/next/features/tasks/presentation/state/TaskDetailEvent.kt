package com.example.next.features.tasks.presentation.state

import com.example.next.features.tasks.domain.model.Task

sealed interface TaskDetailEvent {
    data object ToggleCompletion : TaskDetailEvent
    data object DeleteTask : TaskDetailEvent
    data object ConfirmDeleteTask : TaskDetailEvent
    data object CancelDeleteTask : TaskDetailEvent
    data class SaveTask(val task: Task) : TaskDetailEvent
    data object ShowEditDialog : TaskDetailEvent
    data object DismissDialog : TaskDetailEvent
    data object Retry : TaskDetailEvent
}
