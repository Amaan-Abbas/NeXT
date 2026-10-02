package com.example.next.core.navigation

sealed class Screen(val route: String) {
    data object Tasks : Screen("tasks")
    data object Pomodoro : Screen("pomodoro?taskId={taskId}") {
        fun createRoute(taskId: String? = null): String {
            return if (!taskId.isNullOrEmpty()) "pomodoro?taskId=$taskId" else "pomodoro"
        }
    }
    data object TaskDetail : Screen("taskDetail/{taskId}") {
        fun createRoute(taskId: String): String = "taskDetail/$taskId"
    }
    data object Home : Screen("home")
    data object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: String): String = "detail/$itemId"
    }
    data object Settings : Screen("settings")
}
