package com.example.next.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.next.features.tasks.domain.model.Task
import com.example.next.ui.theme.PomodoroFocusColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TaskCard(
    task: Task,
    onToggleComplete: (String) -> Unit,
    onStartPomodoro: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    modifier: Modifier = Modifier,
    isActivePomodoro: Boolean = false
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val now = remember { System.currentTimeMillis() }
    val startOfToday = remember(now) {
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val isOverdue = remember(task.dueDate, task.isCompleted, startOfToday) {
        !task.isCompleted && task.dueDate != null && task.dueDate < startOfToday
    }

    val cardContainerColor by animateColorAsState(
        targetValue = when {
            isActivePomodoro -> PomodoroFocusColor.copy(alpha = 0.08f)
            task.isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "CardContainerColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTaskClick(task.id) },
        shape = MaterialTheme.shapes.medium,
        border = if (isActivePomodoro) BorderStroke(2.dp, PomodoroFocusColor) else null,
        colors = CardDefaults.cardColors(containerColor = cardContainerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .alpha(if (task.isCompleted) 0.75f else 1.0f)
        ) {
            if (isActivePomodoro) {
                Surface(
                    shape = CircleShape,
                    color = PomodoroFocusColor.copy(alpha = 0.2f),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Active Pomodoro",
                            tint = PomodoroFocusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "POMODORO RUNNING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PomodoroFocusColor
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete(task.id) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                PriorityChip(priority = task.priority)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Pomodoro sessions indicator badge
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Pomodoro progress",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${task.completedPomodoros}/${task.estimatedPomodoros}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Due date/time indicator badge
                    if (task.dueDate != null) {
                        val badgeBg = if (isOverdue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                        val badgeBorder = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        val badgeTint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = badgeBg,
                            border = BorderStroke(1.dp, badgeBorder)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        isOverdue -> Icons.Default.Warning
                                        task.dueTime != null -> Icons.Default.Schedule
                                        else -> Icons.Default.Event
                                    },
                                    contentDescription = if (isOverdue) "Overdue task" else "Due date",
                                    tint = badgeTint,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                val dateText = buildString {
                                    if (isOverdue) append("Overdue: ")
                                    append(dateFormat.format(Date(task.dueDate)))
                                    if (!task.dueTime.isNullOrBlank()) {
                                        append(" ").append(task.dueTime)
                                    }
                                }
                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Medium,
                                    color = badgeTint,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onDeleteTask(task.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Task",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    AppPrimaryButton(
                        onClick = { onStartPomodoro(task.id) },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Focus",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
