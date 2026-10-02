package com.example.next.features.pomodoro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.next.core.common.Result
import com.example.next.features.pomodoro.domain.engine.PomodoroTimerEngine
import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.pomodoro.domain.model.TimerState
import com.example.next.features.pomodoro.domain.usecase.GetPomodoroSessionsUseCase
import com.example.next.features.pomodoro.presentation.state.PomodoroEvent
import com.example.next.features.pomodoro.presentation.state.PomodoroUiState
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.repository.TaskRepository
import com.example.next.features.tasks.domain.usecase.GetTaskByIdUseCase
import com.example.next.features.tasks.domain.usecase.GetTasksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PomodoroViewModel(
    private val initialTaskId: String?,
    private val timerEngine: PomodoroTimerEngine,
    private val getTasksUseCase: GetTasksUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val getPomodoroSessionsUseCase: GetPomodoroSessionsUseCase,
    private val taskRepository: TaskRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    init {
        observeTimerEngine()
        loadTasks()
        loadSessionsCount()
        if (!initialTaskId.isNullOrBlank()) {
            val engineState = timerEngine.engineState.value
            val isActiveOnOtherTask = (engineState.timerState == TimerState.RUNNING || engineState.timerState == TimerState.PAUSED) &&
                    engineState.selectedTaskId != null && engineState.selectedTaskId != initialTaskId
            if (!isActiveOnOtherTask) {
                loadInitialTask(initialTaskId)
                timerEngine.selectTask(initialTaskId)
            } else {
                loadInitialTask(engineState.selectedTaskId!!)
            }
        } else {
            val currentEngineTaskId = timerEngine.engineState.value.selectedTaskId
            if (!currentEngineTaskId.isNullOrBlank()) {
                loadInitialTask(currentEngineTaskId)
            }
        }
    }

    fun onEvent(event: PomodoroEvent) {
        when (event) {
            is PomodoroEvent.StartTimer -> timerEngine.start()
            is PomodoroEvent.PauseTimer -> timerEngine.pause()
            is PomodoroEvent.ResumeTimer -> timerEngine.resume()
            is PomodoroEvent.ResetTimer -> timerEngine.reset()
            is PomodoroEvent.SkipTimer -> timerEngine.skip()
            is PomodoroEvent.SelectMode -> timerEngine.selectMode(event.mode)
            is PomodoroEvent.SelectTask -> {
                _uiState.update { it.copy(selectedTask = event.task, isTaskSelectorOpen = false) }
                timerEngine.selectTask(event.task?.id)
            }
            is PomodoroEvent.SetInitialTaskId -> event.taskId?.let { loadInitialTask(it) }
            is PomodoroEvent.QuickAddEstimate -> quickAddEstimate(event.taskId, event.count)
            is PomodoroEvent.ToggleTaskSelector -> _uiState.update { it.copy(isTaskSelectorOpen = event.isOpen) }
            is PomodoroEvent.SearchTasks -> _uiState.update { it.copy(taskSearchQuery = event.query) }
            is PomodoroEvent.DismissCompletionFeedback -> _uiState.update { it.copy(completionEventMessage = null) }
        }
    }

    private fun observeTimerEngine() {
        var previousState: TimerState = TimerState.IDLE
        viewModelScope.launch {
            timerEngine.engineState.collect { engineState ->
                val wasJustCompleted = previousState != TimerState.COMPLETED && engineState.timerState == TimerState.COMPLETED
                previousState = engineState.timerState

                val completionMessage = if (wasJustCompleted) {
                    when (engineState.mode) {
                        PomodoroMode.FOCUS -> "Focus session completed! Take a break."
                        PomodoroMode.SHORT_BREAK -> "Short break finished. Ready to get back to focus!"
                        PomodoroMode.LONG_BREAK -> "Long break completed! Refreshed and ready for the next round."
                    }
                } else null

                _uiState.update { current ->
                    val syncSelectedTask = if (engineState.selectedTaskId == null) {
                        null
                    } else {
                        current.availableTasks.find { it.id == engineState.selectedTaskId } ?: current.selectedTask
                    }

                    current.copy(
                        mode = engineState.mode,
                        timerState = engineState.timerState,
                        timeRemainingSeconds = engineState.timeRemainingSeconds,
                        totalDurationSeconds = engineState.totalDurationSeconds,
                        completedSessionsCount = engineState.completedFocusCount,
                        sessionsPerLongBreak = engineState.config.sessionsPerLongBreak,
                        completionEventMessage = completionMessage ?: current.completionEventMessage,
                        selectedTask = syncSelectedTask
                    )
                }
            }
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            getTasksUseCase().collect { result ->
                if (result is Result.Success) {
                    val tasks = result.data
                    val currentEngineSelectedId = timerEngine.engineState.value.selectedTaskId
                    _uiState.update { current ->
                        val updatedSelected = if (currentEngineSelectedId != null) {
                            tasks.find { it.id == currentEngineSelectedId } ?: current.selectedTask
                        } else {
                            current.selectedTask?.let { sel -> tasks.find { it.id == sel.id } }
                        }
                        current.copy(availableTasks = tasks, selectedTask = updatedSelected)
                    }
                }
            }
        }
    }

    private fun loadSessionsCount() {
        viewModelScope.launch {
            getPomodoroSessionsUseCase().collect { result ->
                if (result is Result.Success) {
                    val count = result.data.count { it.isCompleted && it.sessionType == PomodoroMode.FOCUS }
                    _uiState.update { it.copy(completedSessionsCount = count) }
                }
            }
        }
    }

    private fun loadInitialTask(taskId: String) {
        viewModelScope.launch {
            getTaskByIdUseCase(taskId).collect { result ->
                if (result is Result.Success && result.data != null) {
                    _uiState.update { it.copy(selectedTask = result.data) }
                    val currentEngineState = timerEngine.engineState.value
                    val isActiveOnOtherTask = (currentEngineState.timerState == TimerState.RUNNING || currentEngineState.timerState == TimerState.PAUSED) &&
                            currentEngineState.selectedTaskId != null && currentEngineState.selectedTaskId != taskId
                    if (!isActiveOnOtherTask) {
                        timerEngine.selectTask(taskId)
                    }
                }
            }
        }
    }

    private fun quickAddEstimate(taskId: String, count: Int) {
        if (taskRepository == null) return
        viewModelScope.launch {
            val result = getTaskByIdUseCase(taskId).firstOrNull { it !is Result.Loading }
            if (result is Result.Success && result.data != null) {
                val currentTask = result.data
                val currentEst = currentTask.estimatedPomodoros ?: 0
                val newEst = currentEst + count
                _uiState.update { current ->
                    if (current.selectedTask?.id == taskId) {
                        current.copy(selectedTask = current.selectedTask.copy(estimatedPomodoros = newEst))
                    } else current
                }
                taskRepository.updateEstimatedPomodoros(taskId, newEst)
            }
        }
    }

    class Factory(
        private val initialTaskId: String?,
        private val timerEngine: PomodoroTimerEngine,
        private val getTasksUseCase: GetTasksUseCase,
        private val getTaskByIdUseCase: GetTaskByIdUseCase,
        private val getPomodoroSessionsUseCase: GetPomodoroSessionsUseCase,
        private val taskRepository: TaskRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PomodoroViewModel(
                initialTaskId = initialTaskId,
                timerEngine = timerEngine,
                getTasksUseCase = getTasksUseCase,
                getTaskByIdUseCase = getTaskByIdUseCase,
                getPomodoroSessionsUseCase = getPomodoroSessionsUseCase,
                taskRepository = taskRepository
            ) as T
        }
    }
}
