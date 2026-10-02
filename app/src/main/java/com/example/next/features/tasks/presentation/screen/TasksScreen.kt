package com.example.next.features.tasks.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.next.core.ui.components.AppNavBar
import com.example.next.core.ui.components.AppTopBar
import com.example.next.core.ui.components.ConfirmationDialog
import com.example.next.core.ui.components.EmptyContent
import com.example.next.core.ui.components.ErrorContent
import com.example.next.core.ui.components.LoadingContent
import com.example.next.core.ui.components.TaskCard
import com.example.next.core.ui.components.TaskFormDialog
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskGroup
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.features.tasks.domain.model.TaskSortOption
import com.example.next.features.tasks.presentation.state.TaskTab
import com.example.next.features.tasks.presentation.state.TasksEvent
import com.example.next.features.tasks.presentation.state.TasksUiState
import com.example.next.ui.theme.NeXTTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    uiState: TasksUiState,
    onEvent: (TasksEvent) -> Unit,
    onTaskClick: (String) -> Unit,
    onStartPomodoroForTask: (String) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        val message = uiState.userMessage
        if (message != null) {
            val hasUndo = uiState.recentlyDeletedTask != null
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (hasUndo) "Undo" else null,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed && hasUndo) {
                onEvent(TasksEvent.UndoDeleteTask)
            }
            onEvent(TasksEvent.ClearUserMessage)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = "Tasks & Goals",
                canNavigateBack = false
            )
        },
        bottomBar = {
            AppNavBar(
                currentRoute = "tasks",
                onNavigateToRoute = onNavigateToRoute
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(TasksEvent.ShowAddDialog) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(TasksEvent.UpdateSearchQuery(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search tasks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onEvent(TasksEvent.UpdateSearchQuery("")) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            val activeTaskCount = remember(uiState.tasks) { uiState.tasks.count { !it.isCompleted } }
            val completedTaskCount = remember(uiState.tasks) { uiState.tasks.count { it.isCompleted } }

            // Task View Mode Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedTab == TaskTab.ACTIVE,
                    onClick = { onEvent(TasksEvent.SelectTab(TaskTab.ACTIVE)) },
                    leadingIcon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    label = { Text("Incomplete ($activeTaskCount)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.selectedTab == TaskTab.COMPLETED,
                    onClick = { onEvent(TasksEvent.SelectTab(TaskTab.COMPLETED)) },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text("Completed ($completedTaskCount)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Priority Filter & Sort Row (Shown for Active Tasks)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedPriorityFilter == null,
                            onClick = { onEvent(TasksEvent.FilterByPriority(null)) },
                            label = { Text("All Priorities") }
                        )
                    }
                    items(TaskPriority.entries.toTypedArray()) { priority ->
                        FilterChip(
                            selected = uiState.selectedPriorityFilter == priority,
                            onClick = { onEvent(TasksEvent.FilterByPriority(priority)) },
                            label = { Text(priority.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sort Selector Dropdown
                var isSortMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { isSortMenuExpanded = true },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        label = { Text(uiState.selectedSortOption.label) }
                    )
                    DropdownMenu(
                        expanded = isSortMenuExpanded,
                        onDismissRequest = { isSortMenuExpanded = false }
                    ) {
                        TaskSortOption.entries.forEach { sortOpt ->
                            DropdownMenuItem(
                                text = { Text(sortOpt.label) },
                                onClick = {
                                    onEvent(TasksEvent.SelectSortOption(sortOpt))
                                    isSortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val displayedTasks = remember(uiState.tasks, uiState.selectedTab) {
                when (uiState.selectedTab) {
                    TaskTab.ACTIVE -> uiState.tasks.filter { !it.isCompleted }
                    TaskTab.COMPLETED -> uiState.tasks.filter { it.isCompleted }
                }
            }

            val listState = rememberLazyListState()
            var previousTaskCount by remember { mutableIntStateOf(displayedTasks.size) }

            LaunchedEffect(displayedTasks.size) {
                if (displayedTasks.size > previousTaskCount) {
                    listState.animateScrollToItem(0)
                }
                previousTaskCount = displayedTasks.size
            }

            when {
                uiState.isLoading -> {
                    LoadingContent(modifier = Modifier.weight(1f))
                }
                uiState.error != null -> {
                    ErrorContent(
                        errorMessage = uiState.error,
                        onRetry = { onEvent(TasksEvent.LoadTasks) },
                        modifier = Modifier.weight(1f)
                    )
                }
                displayedTasks.isEmpty() -> {
                    EmptyContent(
                        title = when (uiState.selectedTab) {
                            TaskTab.ACTIVE -> "No Incomplete Tasks"
                            TaskTab.COMPLETED -> "No Completed Tasks"
                        },
                        message = when (uiState.selectedTab) {
                            TaskTab.ACTIVE -> {
                                if (uiState.searchQuery.isNotBlank() || uiState.selectedPriorityFilter != null) {
                                    "No incomplete tasks match the active search or priority filter."
                                } else {
                                    "Your task list is empty. Tap below to create your first task!"
                                }
                            }
                            TaskTab.COMPLETED -> {
                                if (uiState.searchQuery.isNotBlank()) {
                                    "No completed tasks match your search query."
                                } else {
                                    "No completed tasks yet. Finish a task to see it here!"
                                }
                            }
                        },
                        actionLabel = if (uiState.selectedTab == TaskTab.ACTIVE && uiState.searchQuery.isBlank() && uiState.selectedPriorityFilter == null) "Create Task" else null,
                        onActionClick = if (uiState.selectedTab == TaskTab.ACTIVE && uiState.searchQuery.isBlank() && uiState.selectedPriorityFilter == null) {
                            { onEvent(TasksEvent.ShowAddDialog) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> {
                    val now = remember { System.currentTimeMillis() }
                    val (startOfTodayMillis, endOfTodayMillis) = remember(now) {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = now
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val start = cal.timeInMillis
                        cal.set(Calendar.HOUR_OF_DAY, 23)
                        cal.set(Calendar.MINUTE, 59)
                        cal.set(Calendar.SECOND, 59)
                        cal.set(Calendar.MILLISECOND, 999)
                        val end = cal.timeInMillis
                        Pair(start, end)
                    }

                    val showGrouping = uiState.selectedTab == TaskTab.ACTIVE && uiState.selectedSortOption == TaskSortOption.DUE_DATE

                    if (showGrouping) {
                        val groupedMap = remember(displayedTasks, startOfTodayMillis, endOfTodayMillis) {
                            displayedTasks.groupBy { task ->
                                TaskGroup.getGroupForTask(task, startOfTodayMillis, endOfTodayMillis)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            TaskGroup.entries.forEach { group ->
                                val groupTasks = groupedMap[group] ?: emptyList()
                                if (groupTasks.isNotEmpty()) {
                                    item(key = "header_${group.name}") {
                                        androidx.compose.material3.Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = when (group) {
                                                TaskGroup.OVERDUE -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                                TaskGroup.TODAY -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 10.dp, bottom = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = group.title.uppercase(),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (group) {
                                                        TaskGroup.OVERDUE -> MaterialTheme.colorScheme.error
                                                        TaskGroup.TODAY -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                    }
                                                )
                                                androidx.compose.material3.Surface(
                                                    shape = androidx.compose.foundation.shape.CircleShape,
                                                    color = when (group) {
                                                        TaskGroup.OVERDUE -> MaterialTheme.colorScheme.error
                                                        TaskGroup.TODAY -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.outline
                                                    }
                                                ) {
                                                    Text(
                                                        text = "${groupTasks.size}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.surface,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                     items(groupTasks, key = { it.id }) { task ->
                                        TaskCard(
                                            task = task,
                                            onToggleComplete = { onEvent(TasksEvent.ToggleTaskCompletion(it)) },
                                            onStartPomodoro = { onStartPomodoroForTask(it) },
                                            onTaskClick = { onTaskClick(it) },
                                            onDeleteTask = { onEvent(TasksEvent.DeleteTask(it)) },
                                            isActivePomodoro = (task.id == uiState.activePomodoroTaskId && uiState.isPomodoroActive)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(displayedTasks, key = { it.id }) { task ->
                                TaskCard(
                                    task = task,
                                    onToggleComplete = { onEvent(TasksEvent.ToggleTaskCompletion(it)) },
                                    onStartPomodoro = { onStartPomodoroForTask(it) },
                                    onTaskClick = { onTaskClick(it) },
                                    onDeleteTask = { onEvent(TasksEvent.DeleteTask(it)) },
                                    isActivePomodoro = (task.id == uiState.activePomodoroTaskId && uiState.isPomodoroActive)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.isAddEditDialogShowing) {
        TaskFormDialog(
            task = uiState.editingTask,
            onDismiss = { onEvent(TasksEvent.DismissDialog) },
            onSave = { task -> onEvent(TasksEvent.SaveTask(task)) }
        )
    }

    if (uiState.taskPendingDeletion != null) {
        ConfirmationDialog(
            title = "Delete Task",
            message = "Are you sure you want to delete '${uiState.taskPendingDeletion.title}'? This task is incomplete.",
            onConfirm = { onEvent(TasksEvent.ConfirmDeleteTask) },
            onDismiss = { onEvent(TasksEvent.CancelDeleteTask) },
            confirmText = "Delete",
            dismissText = "Cancel"
        )
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun TasksScreenPreview() {
    val sampleTasks = listOf(
        Task(
            id = "task-1",
            title = "Implement Persistent Room Data Layer",
            description = "Build Task entity, DAO, Database integration, and Mappers.",
            priority = TaskPriority.HIGH,
            estimatedPomodoros = 3,
            completedPomodoros = 1
        ),
        Task(
            id = "task-2",
            title = "Design Settings UI & Preferences",
            description = "Dark theme, dynamic color toggle, auto-delete policy settings.",
            priority = TaskPriority.MEDIUM,
            estimatedPomodoros = 2,
            completedPomodoros = 2,
            isCompleted = true
        )
    )

    NeXTTheme {
        TasksScreen(
            uiState = TasksUiState(
                isLoading = false,
                tasks = sampleTasks,
                selectedTab = TaskTab.ACTIVE,
                selectedSortOption = TaskSortOption.DUE_DATE
            ),
            onEvent = {},
            onTaskClick = {},
            onStartPomodoroForTask = {},
            onNavigateToRoute = {}
        )
    }
}


