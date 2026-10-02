package com.example.next.features.tasks.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.next.core.ui.components.AppPrimaryButton
import com.example.next.core.ui.components.AppSecondaryButton
import com.example.next.core.ui.components.AppTopBar
import com.example.next.core.ui.components.ConfirmationDialog
import com.example.next.core.ui.components.ErrorContent
import com.example.next.core.ui.components.LoadingContent
import com.example.next.core.ui.components.PriorityChip
import com.example.next.core.ui.components.TaskFormDialog
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.features.tasks.presentation.state.TaskDetailEvent
import com.example.next.features.tasks.presentation.state.TaskDetailUiState
import com.example.next.ui.theme.NeXTTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    onEvent: (TaskDetailEvent) -> Unit,
    onStartPomodoro: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = "Task Details",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent(modifier = Modifier.weight(1f))
                }
                uiState.error != null -> {
                    ErrorContent(
                        errorMessage = uiState.error,
                        onRetry = { onEvent(TaskDetailEvent.Retry) },
                        modifier = Modifier.weight(1f)
                    )
                }
                uiState.task != null -> {
                    val task = uiState.task
                    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                    val dateTimeFormat = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PriorityChip(priority = task.priority)
                                Text(
                                    text = if (task.isCompleted) "Completed" else "In Progress",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (task.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Due Date & Time Info
                            if (task.dueDate != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (task.dueTime != null) Icons.Default.Schedule else Icons.Default.Event,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val dueText = buildString {
                                        append("Due ").append(dateFormat.format(Date(task.dueDate)))
                                        if (!task.dueTime.isNullOrBlank()) {
                                            append(" at ").append(task.dueTime)
                                        }
                                    }
                                    Text(
                                        text = dueText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Pomodoro Progress Section
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val est = task.estimatedPomodoros
                                val comp = task.completedPomodoros
                                val progressText = if (est == null) {
                                    "$comp Sessions Completed"
                                } else if (comp > est) {
                                    "$comp / $est (+${comp - est} Extra) Sessions Completed"
                                } else {
                                    "$comp / $est Sessions Completed"
                                }

                                Text(
                                    text = progressText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val progressFraction = remember(task.completedPomodoros, task.estimatedPomodoros) {
                                val est = task.estimatedPomodoros
                                if (est != null && est > 0) {
                                    (task.completedPomodoros.toFloat() / est.toFloat()).coerceIn(0f, 1f)
                                } else 1f
                            }

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(MaterialTheme.shapes.small),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface,
                                strokeCap = StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Created on ${dateTimeFormat.format(Date(task.createdAt))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions
                    AppPrimaryButton(
                        onClick = { onStartPomodoro(task.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Pomodoro Focus Session")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AppSecondaryButton(
                        onClick = { onEvent(TaskDetailEvent.ToggleCompletion) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (task.isCompleted) "Mark Task as Incomplete" else "Mark Task as Complete")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppSecondaryButton(
                            onClick = { onEvent(TaskDetailEvent.ShowEditDialog) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Task")
                        }

                        OutlinedButton(
                            onClick = { onEvent(TaskDetailEvent.DeleteTask) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Delete Task",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.isEditDialogShowing && uiState.task != null) {
        TaskFormDialog(
            task = uiState.task,
            onDismiss = { onEvent(TaskDetailEvent.DismissDialog) },
            onSave = { task -> onEvent(TaskDetailEvent.SaveTask(task)) }
        )
    }

    if (uiState.isDeleteConfirmationShowing && uiState.task != null) {
        ConfirmationDialog(
            title = "Delete Task",
            message = "Are you sure you want to delete '${uiState.task.title}'? This task is incomplete.",
            onConfirm = { onEvent(TaskDetailEvent.ConfirmDeleteTask) },
            onDismiss = { onEvent(TaskDetailEvent.CancelDeleteTask) },
            confirmText = "Delete",
            dismissText = "Cancel"
        )
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun TaskDetailScreenPreview() {
    val sampleTask = Task(
        id = "task-1",
        title = "Implement Jetpack Compose Previews",
        description = "Add preview composables to all screens in the project for both light and dark themes.",
        priority = TaskPriority.HIGH,
        dueDate = System.currentTimeMillis() + 86400000,
        dueTime = "05:00 PM",
        estimatedPomodoros = 4,
        completedPomodoros = 2
    )

    NeXTTheme {
        TaskDetailScreen(
            uiState = TaskDetailUiState(
                isLoading = false,
                task = sampleTask,
                error = null
            ),
            onEvent = {},
            onStartPomodoro = {},
            onNavigateBack = {}
        )
    }
}


