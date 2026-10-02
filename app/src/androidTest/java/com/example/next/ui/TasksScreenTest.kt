package com.example.next.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.features.tasks.presentation.screen.TasksScreen
import com.example.next.features.tasks.presentation.state.TaskTab
import com.example.next.features.tasks.presentation.state.TasksUiState
import com.example.next.ui.theme.NeXTTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TasksScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tasksScreen_displaysTaskTitleAndActionButtons() {
        val testTasks = listOf(
            Task(
                id = "t1",
                title = "Compose UI Automated Testing Task",
                description = "Ensure task renders properly in Compose UI test rule",
                priority = TaskPriority.HIGH,
                estimatedPomodoros = 3,
                completedPomodoros = 1
            )
        )

        composeTestRule.setContent {
            NeXTTheme {
                TasksScreen(
                    uiState = TasksUiState(
                        isLoading = false,
                        tasks = testTasks,
                        selectedTab = TaskTab.ACTIVE
                    ),
                    onEvent = {},
                    onTaskClick = {},
                    onStartPomodoroForTask = {},
                    onNavigateToRoute = {}
                )
            }
        }

        // Verify task title is displayed
        composeTestRule.onNodeWithText("Compose UI Automated Testing Task").assertIsDisplayed()

        // Verify FAB button is displayed
        composeTestRule.onNodeWithContentDescription("Add Task").assertIsDisplayed()

        // Verify Focus button is displayed
        composeTestRule.onNodeWithText("Focus").assertIsDisplayed()
    }

    @Test
    fun tasksScreen_clickingAddFab_triggersOnEvent() {
        var eventTriggered = false

        composeTestRule.setContent {
            NeXTTheme {
                TasksScreen(
                    uiState = TasksUiState(
                        isLoading = false,
                        tasks = emptyList(),
                        selectedTab = TaskTab.ACTIVE
                    ),
                    onEvent = { eventTriggered = true },
                    onTaskClick = {},
                    onStartPomodoroForTask = {},
                    onNavigateToRoute = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        assert(eventTriggered)
    }
}
