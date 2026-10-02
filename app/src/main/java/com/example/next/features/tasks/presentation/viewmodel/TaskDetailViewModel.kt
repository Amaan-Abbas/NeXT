package com.example.next.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.usecase.DeleteTaskUseCase
import com.example.next.features.tasks.domain.usecase.GetTaskByIdUseCase
import com.example.next.features.tasks.domain.usecase.SaveTaskUseCase
import com.example.next.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import com.example.next.features.tasks.presentation.state.TaskDetailEvent
import com.example.next.features.tasks.presentation.state.TaskDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskId: String,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        loadTaskDetail()
    }

    fun onEvent(event: TaskDetailEvent) {
        when (event) {
            is TaskDetailEvent.ToggleCompletion -> toggleTaskCompletion()
            is TaskDetailEvent.DeleteTask -> onRequestDeleteTask()
            is TaskDetailEvent.ConfirmDeleteTask -> confirmDeleteTask()
            is TaskDetailEvent.CancelDeleteTask -> cancelDeleteTask()
            is TaskDetailEvent.SaveTask -> saveTask(event.task)
            is TaskDetailEvent.ShowEditDialog -> _uiState.update { it.copy(isEditDialogShowing = true) }
            is TaskDetailEvent.DismissDialog -> _uiState.update { it.copy(isEditDialogShowing = false) }
            is TaskDetailEvent.Retry -> loadTaskDetail()
        }
    }

    private fun loadTaskDetail() {
        viewModelScope.launch {
            getTaskByIdUseCase(taskId).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        val task = result.data
                        if (task != null) {
                            _uiState.update { it.copy(isLoading = false, task = task, error = null) }
                        } else {
                            _uiState.update { it.copy(isLoading = false, error = "Task not found") }
                        }
                    }
                    is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to load task details") }
                }
            }
        }
    }

    private fun toggleTaskCompletion() {
        viewModelScope.launch {
            toggleTaskCompletionUseCase(taskId)
            loadTaskDetail()
        }
    }

    private fun onRequestDeleteTask() {
        val task = _uiState.value.task ?: return
        if (!task.isCompleted) {
            _uiState.update { it.copy(isDeleteConfirmationShowing = true) }
        } else {
            performDeleteTask()
        }
    }

    private fun confirmDeleteTask() {
        _uiState.update { it.copy(isDeleteConfirmationShowing = false) }
        performDeleteTask()
    }

    private fun cancelDeleteTask() {
        _uiState.update { it.copy(isDeleteConfirmationShowing = false) }
    }

    private fun performDeleteTask() {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    private fun saveTask(task: Task) {
        viewModelScope.launch {
            val result = saveTaskUseCase(task)
            if (result is Result.Success) {
                _uiState.update { it.copy(isEditDialogShowing = false) }
                loadTaskDetail()
            } else if (result is Result.Error) {
                _uiState.update { it.copy(error = result.message ?: "Failed to update task") }
            }
        }
    }

    class Factory(
        private val taskId: String,
        private val getTaskByIdUseCase: GetTaskByIdUseCase,
        private val saveTaskUseCase: SaveTaskUseCase,
        private val deleteTaskUseCase: DeleteTaskUseCase,
        private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskDetailViewModel(
                taskId = taskId,
                getTaskByIdUseCase = getTaskByIdUseCase,
                saveTaskUseCase = saveTaskUseCase,
                deleteTaskUseCase = deleteTaskUseCase,
                toggleTaskCompletionUseCase = toggleTaskCompletionUseCase
            ) as T
        }
    }
}
