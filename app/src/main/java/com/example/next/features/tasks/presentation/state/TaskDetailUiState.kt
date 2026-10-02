package com.example.next.features.tasks.presentation.state

import com.example.next.features.tasks.domain.model.Task

data class TaskDetailUiState(
    val isLoading: Boolean = false,
    val task: Task? = null,
    val error: String? = null,
    val isEditDialogShowing: Boolean = false,
    val isDeleteConfirmationShowing: Boolean = false,
    val isDeleted: Boolean = false
)
