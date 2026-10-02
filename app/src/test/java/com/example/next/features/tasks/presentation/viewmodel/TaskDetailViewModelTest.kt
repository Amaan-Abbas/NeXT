package com.example.next.features.tasks.presentation.viewmodel

import com.example.next.core.common.Result
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.features.tasks.domain.repository.TaskRepository
import com.example.next.features.tasks.domain.usecase.DeleteTaskUseCase
import com.example.next.features.tasks.domain.usecase.GetTaskByIdUseCase
import com.example.next.features.tasks.domain.usecase.SaveTaskUseCase
import com.example.next.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import com.example.next.features.tasks.presentation.state.TaskDetailEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModelTest {

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

    private var currentTask: Task? = incompleteTask
    private var deletedTaskId: String? = null

    private val fakeRepository = object : TaskRepository {
        override fun getTasks(): Flow<Result<List<Task>>> = MutableStateFlow(Result.Success(emptyList()))
        override fun getTaskById(id: String): Flow<Result<Task?>> = MutableStateFlow(Result.Success(currentTask))
        override suspend fun saveTask(task: Task): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTask(id: String): Result<Unit> {
            deletedTaskId = id
            return Result.Success(Unit)
        }
        override suspend fun toggleTaskCompletion(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> = Result.Success(Unit)
        override fun searchTasks(query: String): Flow<Result<List<Task>>> = MutableStateFlow(Result.Success(emptyList()))
        override suspend fun clearAllTasks(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(task: Task): TaskDetailViewModel {
        currentTask = task
        val vm = TaskDetailViewModel(
            taskId = task.id,
            getTaskByIdUseCase = GetTaskByIdUseCase(fakeRepository),
            saveTaskUseCase = SaveTaskUseCase(fakeRepository),
            deleteTaskUseCase = DeleteTaskUseCase(fakeRepository),
            toggleTaskCompletionUseCase = ToggleTaskCompletionUseCase(fakeRepository)
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `deleting incomplete task shows confirmation dialog and does not delete immediately`() {
        val viewModel = createViewModel(incompleteTask)

        viewModel.onEvent(TaskDetailEvent.DeleteTask)

        assertTrue(viewModel.uiState.value.isDeleteConfirmationShowing)
        assertFalse(viewModel.uiState.value.isDeleted)
    }

    @Test
    fun `confirming incomplete task deletion deletes task and updates isDeleted`() {
        val viewModel = createViewModel(incompleteTask)

        viewModel.onEvent(TaskDetailEvent.DeleteTask)
        viewModel.onEvent(TaskDetailEvent.ConfirmDeleteTask)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("t1", deletedTaskId)
        assertFalse(viewModel.uiState.value.isDeleteConfirmationShowing)
        assertTrue(viewModel.uiState.value.isDeleted)
    }

    @Test
    fun `cancelling incomplete task deletion hides confirmation dialog without deleting`() {
        val viewModel = createViewModel(incompleteTask)

        viewModel.onEvent(TaskDetailEvent.DeleteTask)
        viewModel.onEvent(TaskDetailEvent.CancelDeleteTask)

        assertFalse(viewModel.uiState.value.isDeleteConfirmationShowing)
        assertFalse(viewModel.uiState.value.isDeleted)
    }

    @Test
    fun `deleting completed task deletes immediately without showing confirmation dialog`() {
        val viewModel = createViewModel(completedTask)

        viewModel.onEvent(TaskDetailEvent.DeleteTask)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("t2", deletedTaskId)
        assertFalse(viewModel.uiState.value.isDeleteConfirmationShowing)
        assertTrue(viewModel.uiState.value.isDeleted)
    }
}
