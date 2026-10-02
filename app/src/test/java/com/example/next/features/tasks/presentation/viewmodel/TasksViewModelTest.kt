package com.example.next.features.tasks.presentation.viewmodel

import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.features.tasks.domain.model.TaskSortOption
import com.example.next.features.tasks.domain.repository.TaskRepository
import com.example.next.features.tasks.domain.usecase.DeleteTaskUseCase
import com.example.next.features.tasks.domain.usecase.GetTasksUseCase
import com.example.next.features.tasks.domain.usecase.SaveTaskUseCase
import com.example.next.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import com.example.next.features.tasks.presentation.state.TasksEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val incompleteTask = Task(
        id = "t1",
        title = "Incomplete Task",
        description = "Test description",
        priority = TaskPriority.HIGH,
        isCompleted = false
    )

    private val completedTask = Task(
        id = "t2",
        title = "Completed Task",
        description = "Test description",
        priority = TaskPriority.LOW,
        isCompleted = true
    )

    private val tasksFlow = MutableStateFlow<Result<List<Task>>>(
        Result.Success(listOf(incompleteTask, completedTask))
    )

    private var deletedTaskId: String? = null
    private var lastSavedTask: Task? = null

    private val fakeRepository = object : TaskRepository {
        override fun getTasks(): Flow<Result<List<Task>>> = tasksFlow
        override fun getTaskById(id: String): Flow<Result<Task?>> = MutableStateFlow(Result.Success(null))
        override suspend fun saveTask(task: Task): Result<Unit> {
            lastSavedTask = task
            return Result.Success(Unit)
        }
        override suspend fun deleteTask(id: String): Result<Unit> {
            deletedTaskId = id
            return Result.Success(Unit)
        }
        override suspend fun toggleTaskCompletion(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> = Result.Success(Unit)
        override fun searchTasks(query: String): Flow<Result<List<Task>>> = tasksFlow
        override suspend fun clearAllTasks(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    private lateinit var viewModel: TasksViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val getTasksUseCase = GetTasksUseCase(fakeRepository)
        val saveTaskUseCase = SaveTaskUseCase(fakeRepository)
        val deleteTaskUseCase = DeleteTaskUseCase(fakeRepository)
        val toggleTaskCompletionUseCase = ToggleTaskCompletionUseCase(fakeRepository)

        viewModel = TasksViewModel(
            getTasksUseCase = getTasksUseCase,
            saveTaskUseCase = saveTaskUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            toggleTaskCompletionUseCase = toggleTaskCompletionUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deleting incomplete task sets taskPendingDeletion and does not delete immediately`() {
        viewModel.onEvent(TasksEvent.DeleteTask("t1"))

        assertEquals(incompleteTask, viewModel.uiState.value.taskPendingDeletion)
        assertNull(deletedTaskId)
    }

    @Test
    fun `confirming incomplete task deletion executes deletion and sets recentlyDeletedTask`() {
        viewModel.onEvent(TasksEvent.DeleteTask("t1"))
        viewModel.onEvent(TasksEvent.ConfirmDeleteTask)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("t1", deletedTaskId)
        assertEquals(incompleteTask, viewModel.uiState.value.recentlyDeletedTask)
        assertNull(viewModel.uiState.value.taskPendingDeletion)
    }

    @Test
    fun `cancelling incomplete task deletion clears pending task without deleting`() {
        viewModel.onEvent(TasksEvent.DeleteTask("t1"))
        viewModel.onEvent(TasksEvent.CancelDeleteTask)

        assertNull(deletedTaskId)
        assertNull(viewModel.uiState.value.taskPendingDeletion)
    }

    @Test
    fun `deleting completed task deletes immediately without setting pending task`() {
        viewModel.onEvent(TasksEvent.DeleteTask("t2"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("t2", deletedTaskId)
        assertEquals(completedTask, viewModel.uiState.value.recentlyDeletedTask)
        assertNull(viewModel.uiState.value.taskPendingDeletion)
    }

    @Test
    fun `selecting sort option updates uiState`() {
        viewModel.onEvent(TasksEvent.SelectSortOption(TaskSortOption.ALPHABETICAL))

        assertEquals(TaskSortOption.ALPHABETICAL, viewModel.uiState.value.selectedSortOption)
    }

    @Test
    fun `undoing deletion restores recently deleted task`() {
        viewModel.onEvent(TasksEvent.DeleteTask("t2"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(TasksEvent.UndoDeleteTask)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(completedTask, lastSavedTask)
        assertNull(viewModel.uiState.value.recentlyDeletedTask)
    }
}
