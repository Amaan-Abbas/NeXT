package com.example.next.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.next.core.di.AppContainer
import com.example.next.features.detail.presentation.screen.DetailScreen
import com.example.next.features.detail.presentation.viewmodel.DetailViewModel
import com.example.next.features.home.presentation.screen.HomeScreen
import com.example.next.features.home.presentation.viewmodel.HomeViewModel
import com.example.next.features.pomodoro.presentation.screen.PomodoroScreen
import com.example.next.features.pomodoro.presentation.viewmodel.PomodoroViewModel
import com.example.next.features.settings.presentation.screen.SettingsScreen
import com.example.next.features.settings.presentation.viewmodel.SettingsViewModel
import com.example.next.features.tasks.presentation.screen.TaskDetailScreen
import com.example.next.features.tasks.presentation.screen.TasksScreen
import com.example.next.features.tasks.presentation.viewmodel.TaskDetailViewModel
import com.example.next.features.tasks.presentation.viewmodel.TasksViewModel

@Composable
fun AppNavigation(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val onNavigateToTopLevelRoute: (String) -> Unit = { targetRoute ->
        navController.navigate(targetRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Tasks.route,
        modifier = modifier
    ) {
        composable(Screen.Tasks.route) {
            val viewModel: TasksViewModel = viewModel(
                factory = TasksViewModel.Factory(
                    getTasksUseCase = appContainer.getTasksUseCase,
                    saveTaskUseCase = appContainer.saveTaskUseCase,
                    deleteTaskUseCase = appContainer.deleteTaskUseCase,
                    toggleTaskCompletionUseCase = appContainer.toggleTaskCompletionUseCase,
                    timerEngine = appContainer.pomodoroTimerEngine
                )
            )
            val uiState by viewModel.uiState.collectAsState()

            TasksScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onStartPomodoroForTask = { taskId ->
                    navController.navigate(Screen.Pomodoro.createRoute(taskId))
                },
                onNavigateToRoute = onNavigateToTopLevelRoute
            )
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            val viewModel: TaskDetailViewModel = viewModel(
                factory = TaskDetailViewModel.Factory(
                    taskId = taskId,
                    getTaskByIdUseCase = appContainer.getTaskByIdUseCase,
                    saveTaskUseCase = appContainer.saveTaskUseCase,
                    deleteTaskUseCase = appContainer.deleteTaskUseCase,
                    toggleTaskCompletionUseCase = appContainer.toggleTaskCompletionUseCase
                )
            )
            val uiState by viewModel.uiState.collectAsState()

            TaskDetailScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onStartPomodoro = { tId ->
                    navController.navigate(Screen.Pomodoro.createRoute(tId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Pomodoro.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val viewModel: PomodoroViewModel = viewModel(
                factory = PomodoroViewModel.Factory(
                    initialTaskId = taskId,
                    timerEngine = appContainer.pomodoroTimerEngine,
                    getTasksUseCase = appContainer.getTasksUseCase,
                    getTaskByIdUseCase = appContainer.getTaskByIdUseCase,
                    getPomodoroSessionsUseCase = appContainer.getPomodoroSessionsUseCase,
                    taskRepository = appContainer.taskRepository
                )
            )
            val uiState by viewModel.uiState.collectAsState()

            val onNavigateFromPomodoro: (String) -> Unit = { targetRoute ->
                if (targetRoute.startsWith("taskDetail/")) {
                    val previousRoute = navController.previousBackStackEntry?.destination?.route
                    if (previousRoute != null && previousRoute.startsWith("taskDetail/")) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(targetRoute)
                    }
                } else {
                    onNavigateToTopLevelRoute(targetRoute)
                }
            }

            PomodoroScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateToRoute = onNavigateFromPomodoro
            )
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(
                    getHomeItemsUseCase = appContainer.getHomeItemsUseCase,
                    toggleFavoriteUseCase = appContainer.toggleFavoriteUseCase
                )
            )
            val uiState by viewModel.uiState.collectAsState()

            HomeScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onItemClick = { itemId ->
                    navController.navigate(Screen.Detail.createRoute(itemId))
                },
                onNavigateToRoute = onNavigateToTopLevelRoute
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            val viewModel: DetailViewModel = viewModel(
                factory = DetailViewModel.Factory(
                    itemId = itemId,
                    getItemDetailUseCase = appContainer.getItemDetailUseCase,
                    toggleFavoriteUseCase = appContainer.toggleFavoriteUseCase
                )
            )
            val uiState by viewModel.uiState.collectAsState()

            DetailScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(
                    getSettingsUseCase = appContainer.getSettingsUseCase,
                    updateSettingsUseCase = appContainer.updateSettingsUseCase,
                    clearAllAppDataUseCase = appContainer.clearAllAppDataUseCase,
                    autoDeleteOldDataUseCase = appContainer.autoDeleteOldDataUseCase
                )
            )
            val uiState by viewModel.uiState.collectAsState()

            SettingsScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateToRoute = onNavigateToTopLevelRoute
            )
        }
    }
}
