package com.example.next.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    var estimatedPomodoros by remember { mutableStateOf(task?.estimatedPomodoros?.toString() ?: "2") }
    var selectedDueDateMillis by remember { mutableStateOf<Long?>(task?.dueDate) }
    var selectedDueTime by remember { mutableStateOf<String?>(task?.dueTime) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = if (task == null) "Create New Task" else "Edit Task",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = null
                    },
                    label = { Text("Task Title *") },
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Priority", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TaskPriority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = {
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (priority == p) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Due Date & Time", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedDueDateMillis?.let { dateFormat.format(Date(it)) } ?: "Set Date",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    if (selectedDueDateMillis != null) {
                        IconButton(
                            onClick = {
                                selectedDueDateMillis = null
                                selectedDueTime = null
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Date", modifier = Modifier.size(18.dp))
                        }
                    }

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedDueTime ?: "Set Time",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    if (selectedDueTime != null) {
                        IconButton(
                            onClick = { selectedDueTime = null }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Time", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = estimatedPomodoros,
                    onValueChange = { estimatedPomodoros = it.filter { c -> c.isDigit() } },
                    label = { Text("Estimated Pomodoro Sessions") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            AppPrimaryButton(
                onClick = {
                    if (title.isBlank()) {
                        titleError = "Title cannot be empty"
                        return@AppPrimaryButton
                    }
                    val estCount = estimatedPomodoros.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    val updatedTask = task?.copy(
                        title = title.trim(),
                        description = description.trim(),
                        priority = priority,
                        dueDate = selectedDueDateMillis,
                        dueTime = selectedDueTime,
                        estimatedPomodoros = estCount
                    ) ?: Task(
                        id = "",
                        title = title.trim(),
                        description = description.trim(),
                        priority = priority,
                        dueDate = selectedDueDateMillis,
                        dueTime = selectedDueTime,
                        estimatedPomodoros = estCount
                    )
                    onSave(updatedTask)
                },
                modifier = Modifier.height(40.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            AppSecondaryButton(
                onClick = onDismiss,
                modifier = Modifier.height(40.dp)
            ) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDueDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDueDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val hour12 = if (hour % 12 == 0) 12 else hour % 12
                    selectedDueTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            title = { Text("Select Due Time") },
            text = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}
