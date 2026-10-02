package com.example.next.features.settings.presentation.viewmodel

import com.example.next.core.common.Result
import com.example.next.features.home.domain.model.HomeItem
import com.example.next.features.home.domain.repository.HomeRepository
import com.example.next.features.pomodoro.domain.model.PomodoroSession
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import com.example.next.features.settings.domain.model.AutoDeleteUnit
import com.example.next.features.settings.domain.model.UserSettings
import com.example.next.features.settings.domain.repository.SettingsRepository
import com.example.next.features.settings.domain.usecase.AutoDeleteOldDataUseCase
import com.example.next.features.settings.domain.usecase.ClearAllAppDataUseCase
import com.example.next.features.settings.domain.usecase.GetSettingsUseCase
import com.example.next.features.settings.domain.usecase.UpdateSettingsUseCase
import com.example.next.features.settings.presentation.state.SettingsEvent
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.repository.TaskRepository
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
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsFlow = MutableStateFlow<Result<UserSettings>>(Result.Success(UserSettings()))

    private var tasksCleared = false
    private var autoDeleteRun = false

    private val fakeSettingsRepository = object : SettingsRepository {
        override fun getSettings(): Flow<Result<UserSettings>> = settingsFlow
        override suspend fun updateSettings(settings: UserSettings): Result<Unit> {
            settingsFlow.value = Result.Success(settings)
            return Result.Success(Unit)
        }
    }

    private val fakeTaskRepository = object : TaskRepository {
        override fun getTasks(): Flow<Result<List<Task>>> = MutableStateFlow(Result.Success(emptyList()))
        override fun getTaskById(id: String): Flow<Result<Task?>> = MutableStateFlow(Result.Success(null))
        override suspend fun saveTask(task: Task): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteTask(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun toggleTaskCompletion(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun incrementCompletedPomodoros(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun updateEstimatedPomodoros(id: String, newEstimated: Int?): Result<Unit> = Result.Success(Unit)
        override fun searchTasks(query: String): Flow<Result<List<Task>>> = MutableStateFlow(Result.Success(emptyList()))
        override suspend fun clearAllTasks(): Result<Unit> {
            tasksCleared = true
            return Result.Success(Unit)
        }
        override suspend fun deleteTasksOlderThan(cutoffTimestamp: Long): Result<Unit> {
            autoDeleteRun = true
            return Result.Success(Unit)
        }
    }

    private val fakePomodoroRepository = object : PomodoroRepository {
        override fun getSessions(): Flow<Result<List<PomodoroSession>>> = MutableStateFlow(Result.Success(emptyList()))
        override fun getSessionsForTask(taskId: String): Flow<Result<List<PomodoroSession>>> = MutableStateFlow(Result.Success(emptyList()))
        override suspend fun saveSession(session: PomodoroSession): Result<Unit> = Result.Success(Unit)
        override suspend fun clearAllSessions(): Result<Unit> = Result.Success(Unit)
        override suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long): Result<Unit> = Result.Success(Unit)
    }

    private val fakeHomeRepository = object : HomeRepository {
        override fun getHomeItems(): Flow<Result<List<HomeItem>>> = MutableStateFlow(Result.Success(emptyList()))
        override fun getItemById(id: String): Flow<Result<HomeItem>> = MutableStateFlow(Result.Error(message = "Not found"))
        override suspend fun toggleFavorite(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun refreshItems(): Result<Unit> = Result.Success(Unit)
        override suspend fun clearAll(): Result<Unit> = Result.Success(Unit)
    }

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val getSettingsUseCase = GetSettingsUseCase(fakeSettingsRepository)
        val updateSettingsUseCase = UpdateSettingsUseCase(fakeSettingsRepository)
        val clearAllAppDataUseCase = ClearAllAppDataUseCase(fakeTaskRepository, fakePomodoroRepository, fakeHomeRepository)
        val autoDeleteOldDataUseCase = AutoDeleteOldDataUseCase(fakeTaskRepository, fakePomodoroRepository)

        viewModel = SettingsViewModel(
            getSettingsUseCase = getSettingsUseCase,
            updateSettingsUseCase = updateSettingsUseCase,
            clearAllAppDataUseCase = clearAllAppDataUseCase,
            autoDeleteOldDataUseCase = autoDeleteOldDataUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ShowClearAllDataConfirmation shows confirmation dialog`() {
        viewModel.onEvent(SettingsEvent.ShowClearAllDataConfirmation)

        assertTrue(viewModel.uiState.value.isClearAllDataConfirmationShowing)
    }

    @Test
    fun `ConfirmClearAllData executes clear all and sets userMessage`() {
        viewModel.onEvent(SettingsEvent.ConfirmClearAllData)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(tasksCleared)
        assertFalse(viewModel.uiState.value.isClearAllDataConfirmationShowing)
        assertEquals("All application data deleted successfully.", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `UpdateAutoDeleteSettings updates policy and runs auto delete`() {
        viewModel.onEvent(SettingsEvent.UpdateAutoDeleteSettings(AutoDeleteUnit.WEEKS, 2))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AutoDeleteUnit.WEEKS, viewModel.uiState.value.settings.autoDeleteUnit)
        assertEquals(2, viewModel.uiState.value.settings.autoDeleteValue)
        assertTrue(autoDeleteRun)
    }
}
