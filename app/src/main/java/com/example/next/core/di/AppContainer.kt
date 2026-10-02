package com.example.next.core.di

import android.content.Context
import com.example.next.core.common.DefaultDispatcherProvider
import com.example.next.core.common.DispatcherProvider
import com.example.next.data.local.AppDatabase
import com.example.next.data.local.dao.PomodoroSessionDao
import com.example.next.data.local.dao.TaskDao
import com.example.next.features.home.data.datasource.HomeLocalDataSource
import com.example.next.features.home.data.datasource.HomeRemoteDataSource
import com.example.next.features.home.data.repository.HomeRepositoryImpl
import com.example.next.features.home.domain.repository.HomeRepository
import com.example.next.features.home.domain.usecase.GetHomeItemsUseCase
import com.example.next.features.home.domain.usecase.GetItemDetailUseCase
import com.example.next.features.home.domain.usecase.ToggleFavoriteUseCase
import com.example.next.features.pomodoro.data.datasource.PomodoroLocalDataSource
import com.example.next.features.pomodoro.data.repository.PomodoroRepositoryImpl
import com.example.next.features.pomodoro.domain.engine.PomodoroTimerEngine
import com.example.next.features.pomodoro.domain.model.PomodoroConfig
import com.example.next.features.pomodoro.domain.repository.PomodoroRepository
import com.example.next.features.pomodoro.domain.usecase.GetPomodoroHistoryUseCase
import com.example.next.features.pomodoro.domain.usecase.GetPomodoroSessionsUseCase
import com.example.next.features.pomodoro.domain.usecase.PausePomodoroUseCase
import com.example.next.features.pomodoro.domain.usecase.ResetPomodoroUseCase
import com.example.next.features.pomodoro.domain.usecase.ResumePomodoroUseCase
import com.example.next.features.pomodoro.domain.usecase.SavePomodoroSessionUseCase
import com.example.next.features.pomodoro.domain.usecase.SkipPomodoroUseCase
import com.example.next.features.pomodoro.domain.usecase.StartPomodoroUseCase
import com.example.next.features.settings.data.repository.SettingsRepositoryImpl
import com.example.next.features.settings.domain.repository.SettingsRepository
import com.example.next.features.settings.domain.usecase.AutoDeleteOldDataUseCase
import com.example.next.features.settings.domain.usecase.ClearAllAppDataUseCase
import com.example.next.features.settings.domain.usecase.GetSettingsUseCase
import com.example.next.features.settings.domain.usecase.UpdateSettingsUseCase
import com.example.next.features.tasks.data.datasource.TaskLocalDataSource
import com.example.next.features.tasks.data.repository.TaskRepositoryImpl
import com.example.next.features.tasks.domain.repository.TaskRepository
import com.example.next.features.tasks.domain.usecase.CreateTaskUseCase
import com.example.next.features.tasks.domain.usecase.DeleteTaskUseCase
import com.example.next.features.tasks.domain.usecase.GetTaskByIdUseCase
import com.example.next.features.tasks.domain.usecase.GetTasksUseCase
import com.example.next.features.tasks.domain.usecase.SaveTaskUseCase
import com.example.next.features.tasks.domain.usecase.SearchTasksUseCase
import com.example.next.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import com.example.next.features.tasks.domain.usecase.UpdateTaskUseCase

class AppContainer(
    private val context: Context? = null
) {

    val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }

    // Room Database
    val appDatabase: AppDatabase? by lazy {
        context?.let { AppDatabase.getInstance(it) }
    }

    val taskDao: TaskDao? by lazy {
        appDatabase?.taskDao()
    }

    val pomodoroSessionDao: PomodoroSessionDao? by lazy {
        appDatabase?.pomodoroSessionDao()
    }

    // Existing Home Feature Infrastructure
    private val homeLocalDataSource: HomeLocalDataSource by lazy {
        HomeLocalDataSource()
    }

    private val homeRemoteDataSource: HomeRemoteDataSource by lazy {
        HomeRemoteDataSource()
    }

    val homeRepository: HomeRepository by lazy {
        HomeRepositoryImpl(
            localDataSource = homeLocalDataSource,
            remoteDataSource = homeRemoteDataSource,
            dispatchers = dispatcherProvider
        )
    }

    val getHomeItemsUseCase: GetHomeItemsUseCase by lazy {
        GetHomeItemsUseCase(homeRepository)
    }

    val toggleFavoriteUseCase: ToggleFavoriteUseCase by lazy {
        ToggleFavoriteUseCase(homeRepository)
    }

    val getItemDetailUseCase: GetItemDetailUseCase by lazy {
        GetItemDetailUseCase(homeRepository)
    }

    // Tasks Feature Infrastructure
    val taskLocalDataSource: TaskLocalDataSource by lazy {
        val dao = taskDao ?: throw IllegalStateException("TaskDao is not initialized. AppContainer requires a Context.")
        TaskLocalDataSource(dao)
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(
            localDataSource = taskLocalDataSource,
            dispatchers = dispatcherProvider
        )
    }

    val getTasksUseCase: GetTasksUseCase by lazy {
        GetTasksUseCase(taskRepository)
    }

    val getTaskByIdUseCase: GetTaskByIdUseCase by lazy {
        GetTaskByIdUseCase(taskRepository)
    }

    val saveTaskUseCase: SaveTaskUseCase by lazy {
        SaveTaskUseCase(taskRepository)
    }

    val createTaskUseCase: CreateTaskUseCase by lazy {
        CreateTaskUseCase(taskRepository)
    }

    val updateTaskUseCase: UpdateTaskUseCase by lazy {
        UpdateTaskUseCase(taskRepository)
    }

    val searchTasksUseCase: SearchTasksUseCase by lazy {
        SearchTasksUseCase(taskRepository)
    }

    val deleteTaskUseCase: DeleteTaskUseCase by lazy {
        DeleteTaskUseCase(taskRepository)
    }

    val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase by lazy {
        ToggleTaskCompletionUseCase(taskRepository)
    }

    // Pomodoro Feature Infrastructure
    val pomodoroConfig: PomodoroConfig by lazy {
        PomodoroConfig()
    }

    val pomodoroLocalDataSource: PomodoroLocalDataSource by lazy {
        val dao = pomodoroSessionDao ?: throw IllegalStateException("PomodoroSessionDao is not initialized. AppContainer requires a Context.")
        PomodoroLocalDataSource(dao)
    }

    val pomodoroRepository: PomodoroRepository by lazy {
        PomodoroRepositoryImpl(
            localDataSource = pomodoroLocalDataSource,
            dispatchers = dispatcherProvider
        )
    }

    val getPomodoroSessionsUseCase: GetPomodoroSessionsUseCase by lazy {
        GetPomodoroSessionsUseCase(pomodoroRepository)
    }

    val getPomodoroHistoryUseCase: GetPomodoroHistoryUseCase by lazy {
        GetPomodoroHistoryUseCase(pomodoroRepository)
    }

    val savePomodoroSessionUseCase: SavePomodoroSessionUseCase by lazy {
        SavePomodoroSessionUseCase(
            pomodoroRepository = pomodoroRepository,
            taskRepository = taskRepository,
            config = pomodoroConfig
        )
    }

    val pomodoroTimerEngine: PomodoroTimerEngine by lazy {
        PomodoroTimerEngine(
            savePomodoroSessionUseCase = savePomodoroSessionUseCase,
            config = pomodoroConfig
        )
    }

    val startPomodoroUseCase: StartPomodoroUseCase by lazy {
        StartPomodoroUseCase(pomodoroTimerEngine)
    }

    val pausePomodoroUseCase: PausePomodoroUseCase by lazy {
        PausePomodoroUseCase(pomodoroTimerEngine)
    }

    val resumePomodoroUseCase: ResumePomodoroUseCase by lazy {
        ResumePomodoroUseCase(pomodoroTimerEngine)
    }

    val resetPomodoroUseCase: ResetPomodoroUseCase by lazy {
        ResetPomodoroUseCase(pomodoroTimerEngine)
    }

    val skipPomodoroUseCase: SkipPomodoroUseCase by lazy {
        SkipPomodoroUseCase(pomodoroTimerEngine)
    }

    // Settings Feature Infrastructure
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl()
    }

    val getSettingsUseCase: GetSettingsUseCase by lazy {
        GetSettingsUseCase(settingsRepository)
    }

    val updateSettingsUseCase: UpdateSettingsUseCase by lazy {
        UpdateSettingsUseCase(settingsRepository)
    }

    val clearAllAppDataUseCase: ClearAllAppDataUseCase by lazy {
        ClearAllAppDataUseCase(
            taskRepository = taskRepository,
            pomodoroRepository = pomodoroRepository,
            homeRepository = homeRepository
        )
    }

    val autoDeleteOldDataUseCase: AutoDeleteOldDataUseCase by lazy {
        AutoDeleteOldDataUseCase(
            taskRepository = taskRepository,
            pomodoroRepository = pomodoroRepository
        )
    }
}
