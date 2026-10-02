package com.example.next.features.pomodoro.presentation.viewmodel

import com.example.next.core.common.Result
import com.example.next.features.pomodoro.domain.engine.PomodoroTimerEngine
import com.example.next.features.pomodoro.domain.model.PomodoroConfig
import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.model.TimerState
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import com.example.next.features.pomodoro.domain.usecase.GetPomodoroSessionsUseCase
import com.example.next.features.pomodoro.domain.usecase.SavePomodoroSessionUseCase
import com.example.next.features.pomodoro.presentation.state.PomodoroEvent
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.repository.TaskRepository
import com.example.next.features.tasks.domain.usecase.GetTaskByIdUseCase
import com.example.next.features.tasks.domain.usecase.GetTasksUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PomodoroViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val sampleTask = Task(
        id = "t1",
        title = "Sample Task",
        estimatedPomodoros = 2,
        completedPomodoros = 0
    )

    private val fakePomodoroRepository = object : PomodoroRepository {
        override fun getSessions(): Flow<Result<List<PomodoroSession>>> = flowOf(Result.Success(emptyList()))
        override fun getSessionsForTask(taskId: String): Flow<Result<List<PomodoroSession>>> = flowOf(Result.Success(emptyList()))
        override suspend fun saveSession(session: PomodoroSession): Result<Unit> = Result.Success(Unit)
        override suspend fun clearAllSessions(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    private val fakeTaskRepository = object : TaskRepository {
        override fun getTasks(): Flow<Result<List<Task>>> = flowOf(Result.Success(listOf(sampleTask)))
        override fun getTaskById(id: String): Flow<Result<Task?>> = flowOf(Result.Success(if (id == "t1") sampleTask else null))
        override suspend fun saveTask(task: Task): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTask(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun toggleTaskCompletion(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> = Result.Success(Unit)
        override fun searchTasks(query: String): Flow<Result<List<Task>>> = flowOf(Result.Success(emptyList()))
        override suspend fun clearAllTasks(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    private val savePomodoroSessionUseCase = SavePomodoroSessionUseCase(fakePomodoroRepository)
    private val timerEngine = PomodoroTimerEngine(savePomodoroSessionUseCase, PomodoroConfig(), testScope)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ViewModel initializes UI state from timer engine and available tasks`() {
        val viewModel = PomodoroViewModel(
            initialTaskId = "t1",
            timerEngine = timerEngine,
            getTasksUseCase = GetTasksUseCase(fakeTaskRepository),
            getTaskByIdUseCase = GetTaskByIdUseCase(fakeTaskRepository),
            getPomodoroSessionsUseCase = GetPomodoroSessionsUseCase(fakePomodoroRepository),
            taskRepository = fakeTaskRepository
        )

        val state = viewModel.uiState.value
        assertEquals(PomodoroMode.FOCUS, state.mode)
        assertEquals(TimerState.IDLE, state.timerState)
        assertEquals(1, state.availableTasks.size)
        assertEquals("t1", state.selectedTask?.id)
    }

    @Test
    fun `StartTimer event triggers timerEngine running state`() {
        val viewModel = PomodoroViewModel(
            initialTaskId = null,
            timerEngine = timerEngine,
            getTasksUseCase = GetTasksUseCase(fakeTaskRepository),
            getTaskByIdUseCase = GetTaskByIdUseCase(fakeTaskRepository),
            getPomodoroSessionsUseCase = GetPomodoroSessionsUseCase(fakePomodoroRepository),
            taskRepository = fakeTaskRepository
        )

        viewModel.onEvent(PomodoroEvent.StartTimer)

        val state = viewModel.uiState.value
        assertEquals(TimerState.RUNNING, state.timerState)
    }

    @Test
    fun `QuickAddEstimate increments task estimate exactly once without infinite loop`() {
        var updatedEstValue: Int? = null
        var updateCallCount = 0

        val customTaskRepository = object : TaskRepository {
            override fun getTasks(): Flow<Result<List<Task>>> = flowOf(Result.Success(listOf(sampleTask)))
            override fun getTaskById(id: String): Flow<Result<Task?>> = flowOf(Result.Success(sampleTask))
            override suspend fun saveTask(task: Task): Result<Unit> = Result.Success(Unit)
            override suspend fun deleteTask(id: String): Result<Unit> = Result.Success(Unit)
            override suspend fun toggleTaskCompletion(id: String): Result<Unit> = Result.Success(Unit)
            override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> = Result.Success(Unit)
            override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> {
                updateCallCount++
                updatedEstValue = newEstimated
                return Result.Success(Unit)
            }
            override fun searchTasks(query: String): Flow<Result<List<Task>>> = flowOf(Result.Success(emptyList()))
            override suspend fun clearAllTasks(): Result<Unit> = Result.Success(Unit)
            override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
        }

        val viewModel = PomodoroViewModel(
            initialTaskId = "t1",
            timerEngine = timerEngine,
            getTasksUseCase = GetTasksUseCase(customTaskRepository),
            getTaskByIdUseCase = GetTaskByIdUseCase(customTaskRepository),
            getPomodoroSessionsUseCase = GetPomodoroSessionsUseCase(fakePomodoroRepository),
            taskRepository = customTaskRepository
        )

        viewModel.onEvent(PomodoroEvent.QuickAddEstimate("t1", 1))

        assertEquals(1, updateCallCount)
        assertEquals(3, updatedEstValue)
    }

    @Test
    fun `ToggleTaskSelector and SearchTasks events update UI state correctly`() {
        val viewModel = PomodoroViewModel(
            initialTaskId = null,
            timerEngine = timerEngine,
            getTasksUseCase = GetTasksUseCase(fakeTaskRepository),
            getTaskByIdUseCase = GetTaskByIdUseCase(fakeTaskRepository),
            getPomodoroSessionsUseCase = GetPomodoroSessionsUseCase(fakePomodoroRepository),
            taskRepository = fakeTaskRepository
        )

        viewModel.onEvent(PomodoroEvent.ToggleTaskSelector(true))
        assertEquals(true, viewModel.uiState.value.isTaskSelectorOpen)

        viewModel.onEvent(PomodoroEvent.SearchTasks("DBMS"))
        assertEquals("DBMS", viewModel.uiState.value.taskSearchQuery)

        viewModel.onEvent(PomodoroEvent.SelectTask(sampleTask))
        assertEquals("t1", viewModel.uiState.value.selectedTask?.id)
        assertEquals(false, viewModel.uiState.value.isTaskSelectorOpen)
    }

    @Test
    fun `DismissCompletionFeedback event clears completion message`() {
        val viewModel = PomodoroViewModel(
            initialTaskId = null,
            timerEngine = timerEngine,
            getTasksUseCase = GetTasksUseCase(fakeTaskRepository),
            getTaskByIdUseCase = GetTaskByIdUseCase(fakeTaskRepository),
            getPomodoroSessionsUseCase = GetPomodoroSessionsUseCase(fakePomodoroRepository),
            taskRepository = fakeTaskRepository
        )

        viewModel.onEvent(PomodoroEvent.DismissCompletionFeedback)
        assertEquals(null, viewModel.uiState.value.completionEventMessage)
    }

    @Test
    fun `ViewModel initialized with initialTaskId when timer is active on another task preserves active task`() {
        // Start timer on Task "t1"
        timerEngine.selectTask("t1")
        timerEngine.start()

        // Initialize ViewModel with different initialTaskId "t2"
        val task2 = Task(id = "t2", title = "Task 2")
        val customRepo = object : TaskRepository {
            override fun getTasks(): Flow<Result<List<Task>>> = flowOf(Result.Success(listOf(sampleTask, task2)))
            override fun getTaskById(id: String): Flow<Result<Task?>> = flowOf(Result.Success(if (id == "t1") sampleTask else if (id == "t2") task2 else null))
            override suspend fun saveTask(task: Task): Result<Unit> = Result.Success(Unit)
            override suspend fun deleteTask(id: String): Result<Unit> = Result.Success(Unit)
            override suspend fun toggleTaskCompletion(id: String): Result<Unit> = Result.Success(Unit)
            override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> = Result.Success(Unit)
            override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> = Result.Success(Unit)
            override fun searchTasks(query: String): Flow<Result<List<Task>>> = flowOf(Result.Success(emptyList()))
            override suspend fun clearAllTasks(): Result<Unit> = Result.Success(Unit)
            override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
        }

        val viewModel = PomodoroViewModel(
            initialTaskId = "t2",
            timerEngine = timerEngine,
            getTasksUseCase = GetTasksUseCase(customRepo),
            getTaskByIdUseCase = GetTaskByIdUseCase(customRepo),
            getPomodoroSessionsUseCase = GetPomodoroSessionsUseCase(fakePomodoroRepository),
            taskRepository = customRepo
        )

        // Engine selectedTaskId must remain "t1" because session is active
        assertEquals("t1", timerEngine.engineState.value.selectedTaskId)
        assertEquals("t1", viewModel.uiState.value.selectedTask?.id)

        // Clean up
        timerEngine.reset()
    }

    @Test
    fun `Selecting null task configures standalone session in engine and UI state`() {
        val viewModel = PomodoroViewModel(
            initialTaskId = "t1",
            timerEngine = timerEngine,
            getTasksUseCase = GetTasksUseCase(fakeTaskRepository),
            getTaskByIdUseCase = GetTaskByIdUseCase(fakeTaskRepository),
            getPomodoroSessionsUseCase = GetPomodoroSessionsUseCase(fakePomodoroRepository),
            taskRepository = fakeTaskRepository
        )

        viewModel.onEvent(PomodoroEvent.SelectTask(null))

        assertEquals(null, timerEngine.engineState.value.selectedTaskId)
        assertEquals(null, viewModel.uiState.value.selectedTask)
    }
}

