package com.example.next.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.usecase.DeleteTaskUseCase
import com.example.next.features.tasks.domain.usecase.GetTasksUseCase
import com.example.next.features.tasks.domain.usecase.SaveTaskUseCase
import com.example.next.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import com.example.next.features.tasks.presentation.state.TasksEvent
import com.example.next.features.tasks.presentation.state.TasksUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.next.features.pomodoro.domain.engine.PomodoroTimerEngine
import com.example.next.features.pomodoro.domain.model.TimerState

class TasksViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val timerEngine: PomodoroTimerEngine? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private var loadTasksJob: Job? = null

    init {
        observeTasks()
        observeTimerEngine()
    }

    private fun observeTimerEngine() {
        if (timerEngine == null) return
        viewModelScope.launch {
            timerEngine.engineState.collect { engineState ->
                val isActive = engineState.timerState == TimerState.RUNNING || engineState.timerState == TimerState.PAUSED
                _uiState.update { current ->
                    current.copy(
                        activePomodoroTaskId = engineState.selectedTaskId,
                        isPomodoroActive = isActive
                    )
                }
            }
        }
    }

    fun onEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.LoadTasks -> observeTasks()
            is TasksEvent.SelectTab -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
            }
            is TasksEvent.FilterByPriority -> {
                _uiState.update { it.copy(selectedPriorityFilter = event.priority) }
                observeTasks()
            }
            is TasksEvent.SelectSortOption -> {
                _uiState.update { it.copy(selectedSortOption = event.sortOption) }
                observeTasks()
            }
            is TasksEvent.UpdateSearchQuery -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                observeTasks()
            }
            is TasksEvent.ToggleTaskCompletion -> toggleTaskCompletion(event.taskId)
            is TasksEvent.DeleteTask -> onRequestDeleteTask(event.taskId)
            is TasksEvent.ConfirmDeleteTask -> confirmDeleteTask()
            is TasksEvent.CancelDeleteTask -> cancelDeleteTask()
            is TasksEvent.UndoDeleteTask -> undoDeleteTask()
            is TasksEvent.SaveTask -> saveTask(event.task)
            is TasksEvent.ShowAddDialog -> {
                _uiState.update { it.copy(isAddEditDialogShowing = true, editingTask = null) }
            }
            is TasksEvent.ShowEditDialog -> {
                _uiState.update { it.copy(isAddEditDialogShowing = true, editingTask = event.task) }
            }
            is TasksEvent.DismissDialog -> {
                _uiState.update { it.copy(isAddEditDialogShowing = false, editingTask = null) }
            }
            is TasksEvent.ClearUserMessage -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }

    private fun observeTasks() {
        loadTasksJob?.cancel()
        loadTasksJob = viewModelScope.launch {
            val filter = _uiState.value.selectedPriorityFilter
            val query = _uiState.value.searchQuery
            val sortOption = _uiState.value.selectedSortOption
            getTasksUseCase(
                priorityFilter = filter,
                searchQuery = query,
                sortOption = sortOption
            ).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> _uiState.update { it.copy(isLoading = false, tasks = result.data, error = null) }
                    is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to load tasks") }
                }
            }
        }
    }

    private fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            toggleTaskCompletionUseCase(taskId)
        }
    }

    private fun onRequestDeleteTask(taskId: String) {
        val task = _uiState.value.tasks.find { it.id == taskId } ?: return
        if (!task.isCompleted) {
            _uiState.update { it.copy(taskPendingDeletion = task) }
        } else {
            performDeleteTask(task)
        }
    }

    private fun confirmDeleteTask() {
        val task = _uiState.value.taskPendingDeletion ?: return
        performDeleteTask(task)
        _uiState.update { it.copy(taskPendingDeletion = null) }
    }

    private fun cancelDeleteTask() {
        _uiState.update { it.copy(taskPendingDeletion = null) }
    }

    private fun performDeleteTask(task: Task) {
        viewModelScope.launch {
            _uiState.update { it.copy(recentlyDeletedTask = task, userMessage = "Task '${task.title}' deleted") }
            deleteTaskUseCase(task.id)
        }
    }

    private fun undoDeleteTask() {
        val task = _uiState.value.recentlyDeletedTask ?: return
        viewModelScope.launch {
            saveTaskUseCase(task)
            _uiState.update { it.copy(recentlyDeletedTask = null, userMessage = "Task restored") }
        }
    }

    private fun saveTask(task: Task) {
        viewModelScope.launch {
            val result = saveTaskUseCase(task)
            if (result is Result.Success) {
                _uiState.update { it.copy(isAddEditDialogShowing = false, editingTask = null, userMessage = "Task saved") }
            } else if (result is Result.Error) {
                _uiState.update { it.copy(error = result.message ?: "Failed to save task") }
            }
        }
    }

    class Factory(
        private val getTasksUseCase: GetTasksUseCase,
        private val saveTaskUseCase: SaveTaskUseCase,
        private val deleteTaskUseCase: DeleteTaskUseCase,
        private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
        private val timerEngine: PomodoroTimerEngine? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TasksViewModel(
                getTasksUseCase = getTasksUseCase,
                saveTaskUseCase = saveTaskUseCase,
                deleteTaskUseCase = deleteTaskUseCase,
                toggleTaskCompletionUseCase = toggleTaskCompletionUseCase,
                timerEngine = timerEngine
            ) as T
        }
    }
}
