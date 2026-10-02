package com.example.next.features.pomodoro.presentation.state

import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.tasks.domain.model.Task

sealed interface PomodoroEvent {
    data object StartTimer : PomodoroEvent
    data object PauseTimer : PomodoroEvent
    data object ResumeTimer : PomodoroEvent
    data object ResetTimer : PomodoroEvent
    data object SkipTimer : PomodoroEvent
    data class SelectMode(val mode: PomodoroMode) : PomodoroEvent
    data class SelectTask(val task: Task?) : PomodoroEvent
    data class SetInitialTaskId(val taskId: String?) : PomodoroEvent
    data class QuickAddEstimate(val taskId: String, val count: Int = 1) : PomodoroEvent
    data class ToggleTaskSelector(val isOpen: Boolean) : PomodoroEvent
    data class SearchTasks(val query: String) : PomodoroEvent
    data object DismissCompletionFeedback : PomodoroEvent
}
