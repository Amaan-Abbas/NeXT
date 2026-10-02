package com.example.next.features.pomodoro.presentation.screen

import android.content.Context
import android.content.res.Configuration
import android.media.RingtoneManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.next.core.navigation.Screen
import com.example.next.core.ui.components.AppNavBar
import com.example.next.core.ui.components.AppTopBar
import com.example.next.core.ui.components.ConfirmationDialog
import com.example.next.core.ui.components.PriorityChip
import com.example.next.features.pomodoro.domain.model.PomodoroMode
import com.example.next.features.pomodoro.domain.model.TimerState
import com.example.next.features.pomodoro.presentation.state.PomodoroEvent
import com.example.next.features.pomodoro.presentation.state.PomodoroUiState
import com.example.next.features.tasks.domain.model.Task
import com.example.next.features.tasks.domain.model.TaskPriority
import com.example.next.ui.theme.NeXTTheme
import com.example.next.ui.theme.PomodoroBreakColor
import com.example.next.ui.theme.PomodoroFocusColor
import com.example.next.ui.theme.PomodoroLongBreakColor

private sealed interface PendingTimerAction {
    data class ChangeTask(val targetTask: Task?) : PendingTimerAction
    data class ChangeMode(val targetMode: PomodoroMode) : PendingTimerAction
    data object SkipSession : PendingTimerAction
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    uiState: PomodoroUiState,
    onEvent: (PomodoroEvent) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingTimerAction by remember { mutableStateOf<PendingTimerAction?>(null) }

    val isTimerActive = uiState.timerState == TimerState.RUNNING || uiState.timerState == TimerState.PAUSED

    // Audio & Haptic Completion Trigger
    LaunchedEffect(uiState.completionEventMessage) {
        uiState.completionEventMessage?.let { message ->
            try {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}

            try {
                val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, alertUri)
                ringtone?.play()
            } catch (_: Exception) {}

            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
            onEvent(PomodoroEvent.DismissCompletionFeedback)
        }
    }

    val handleBottomNavClick: (String) -> Unit = { targetRoute ->
        onNavigateToRoute(targetRoute)
    }

    val handleSelectTaskFromSheet: (Task?) -> Unit = { targetTask ->
        val isDifferentTask = targetTask?.id != uiState.selectedTask?.id
        if (isTimerActive && isDifferentTask) {
            onEvent(PomodoroEvent.ToggleTaskSelector(false))
            pendingTimerAction = PendingTimerAction.ChangeTask(targetTask)
        } else {
            onEvent(PomodoroEvent.SelectTask(targetTask))
        }
    }

    val handleSelectMode: (PomodoroMode) -> Unit = { targetMode ->
        if (isTimerActive && targetMode != uiState.mode) {
            pendingTimerAction = PendingTimerAction.ChangeMode(targetMode)
        } else {
            onEvent(PomodoroEvent.SelectMode(targetMode))
        }
    }

    val handleSkipClick: () -> Unit = {
        if (isTimerActive) {
            pendingTimerAction = PendingTimerAction.SkipSession
        } else {
            onEvent(PomodoroEvent.SkipTimer)
        }
    }

    val accentColor = when (uiState.mode) {
        PomodoroMode.FOCUS -> PomodoroFocusColor
        PomodoroMode.SHORT_BREAK -> PomodoroBreakColor
        PomodoroMode.LONG_BREAK -> PomodoroLongBreakColor
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = "Pomodoro Timer",
                canNavigateBack = false
            )
        },
        bottomBar = {
            AppNavBar(
                currentRoute = "pomodoro",
                onNavigateToRoute = handleBottomNavClick
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // Landscape split layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        TimerRingDisplay(
                            uiState = uiState,
                            accentColor = accentColor,
                            modifier = Modifier.size(minOf(this@BoxWithConstraints.maxHeight * 0.7f, 220.dp))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ModeSelectorRow(
                            currentMode = uiState.mode,
                            onSelectMode = handleSelectMode
                        )

                        TaskContextSection(
                            selectedTask = uiState.selectedTask,
                            onOpenSelector = { onEvent(PomodoroEvent.ToggleTaskSelector(true)) },
                            onNavigateToTask = { taskId -> onNavigateToRoute(Screen.TaskDetail.createRoute(taskId)) },
                            onQuickAddTarget = { taskId -> onEvent(PomodoroEvent.QuickAddEstimate(taskId, 1)) }
                        )

                        CycleProgressDots(
                            completedCount = uiState.completedSessionsCount,
                            targetCount = uiState.sessionsPerLongBreak,
                            accentColor = accentColor
                        )

                        ControlsRow(
                            timerState = uiState.timerState,
                            accentColor = accentColor,
                            onEvent = onEvent,
                            onSkipClick = handleSkipClick
                        )
                    }
                }
            } else {
                // Portrait single column layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    ModeSelectorRow(
                        currentMode = uiState.mode,
                        onSelectMode = handleSelectMode
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TaskContextSection(
                        selectedTask = uiState.selectedTask,
                        onOpenSelector = { onEvent(PomodoroEvent.ToggleTaskSelector(true)) },
                        onNavigateToTask = { taskId -> onNavigateToRoute(Screen.TaskDetail.createRoute(taskId)) },
                        onQuickAddTarget = { taskId -> onEvent(PomodoroEvent.QuickAddEstimate(taskId, 1)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TimerRingDisplay(
                        uiState = uiState,
                        accentColor = accentColor,
                        modifier = Modifier.size(250.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CycleProgressDots(
                        completedCount = uiState.completedSessionsCount,
                        targetCount = uiState.sessionsPerLongBreak,
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ControlsRow(
                        timerState = uiState.timerState,
                        accentColor = accentColor,
                        onEvent = onEvent,
                        onSkipClick = handleSkipClick,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }

        // Material 3 Task Selection Bottom Sheet
        if (uiState.isTaskSelectorOpen) {
            TaskSelectionBottomSheet(
                uiState = uiState,
                onDismiss = { onEvent(PomodoroEvent.ToggleTaskSelector(false)) },
                onSelectTask = handleSelectTaskFromSheet,
                onSearchQueryChange = { query -> onEvent(PomodoroEvent.SearchTasks(query)) }
            )
        }

        // Confirmation Warning Dialog when performing active session actions (task change, mode change, skip)
        pendingTimerAction?.let { action ->
            val (dialogTitle, dialogMessage, confirmButtonText) = when (action) {
                is PendingTimerAction.ChangeTask -> Triple(
                    "Reset Active Session?",
                    "Changing the task while a Pomodoro session is active will reset the timer clock and record the current session as interrupted.",
                    "Reset & Change Task"
                )
                is PendingTimerAction.ChangeMode -> Triple(
                    "Reset Active Session?",
                    "Switching break or focus mode while a Pomodoro session is active will reset the timer clock and record the current session as interrupted.",
                    "Reset & Switch Mode"
                )
                is PendingTimerAction.SkipSession -> Triple(
                    "Reset Active Session?",
                    "Skipping while a Pomodoro session is active will reset the timer clock and record the current session as interrupted.",
                    "Reset & Skip"
                )
            }

            ConfirmationDialog(
                title = dialogTitle,
                message = dialogMessage,
                confirmText = confirmButtonText,
                dismissText = "Cancel",
                onConfirm = {
                    when (action) {
                        is PendingTimerAction.ChangeTask -> {
                            onEvent(PomodoroEvent.ResetTimer)
                            onEvent(PomodoroEvent.SelectTask(action.targetTask))
                        }
                        is PendingTimerAction.ChangeMode -> {
                            onEvent(PomodoroEvent.SelectMode(action.targetMode))
                        }
                        is PendingTimerAction.SkipSession -> {
                            onEvent(PomodoroEvent.SkipTimer)
                        }
                    }
                    pendingTimerAction = null
                },
                onDismiss = {
                    pendingTimerAction = null
                }
            )
        }
    }
}

@Composable
private fun ModeSelectorRow(
    currentMode: PomodoroMode,
    onSelectMode: (PomodoroMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        PomodoroMode.entries.forEach { mode ->
            val isSelected = currentMode == mode
            val modeIcon = when (mode) {
                PomodoroMode.FOCUS -> Icons.Default.Timer
                PomodoroMode.SHORT_BREAK -> Icons.Default.CheckCircle
                PomodoroMode.LONG_BREAK -> Icons.Default.Star
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelectMode(mode) },
                label = {
                    Text(
                        text = mode.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = modeIcon,
                        contentDescription = mode.title,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (mode) {
                        PomodoroMode.FOCUS -> PomodoroFocusColor.copy(alpha = 0.2f)
                        PomodoroMode.SHORT_BREAK -> PomodoroBreakColor.copy(alpha = 0.2f)
                        PomodoroMode.LONG_BREAK -> PomodoroLongBreakColor.copy(alpha = 0.2f)
                    },
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                    selectedLeadingIconColor = when (mode) {
                        PomodoroMode.FOCUS -> PomodoroFocusColor
                        PomodoroMode.SHORT_BREAK -> PomodoroBreakColor
                        PomodoroMode.LONG_BREAK -> PomodoroLongBreakColor
                    }
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TaskContextSection(
    selectedTask: Task?,
    onOpenSelector: () -> Unit,
    onNavigateToTask: (String) -> Unit,
    onQuickAddTarget: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedTask != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "FOCUSED TASK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        selectedTask.priority?.let { priority ->
                            PriorityChip(priority = priority)
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenSelector,
                        contentPadding = ButtonDefaults.ContentPadding,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Change Task",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedTask.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToTask(selectedTask.id) }
                    )
                    IconButton(
                        onClick = { onNavigateToTask(selectedTask.id) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View Task Details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val est = selectedTask.estimatedPomodoros
                    val comp = selectedTask.completedPomodoros
                    val progressText = if (est == null) {
                        "Completed: $comp sessions"
                    } else if (comp > est) {
                        "Progress: $comp / $est (+${comp - est} extra)"
                    } else {
                        "Progress: $comp / $est sessions"
                    }

                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AssistChip(
                        onClick = { onQuickAddTarget(selectedTask.id) },
                        label = { Text("+1 Session", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add target",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }
        }
    } else {
        OutlinedCard(
            onClick = onOpenSelector,
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "No Task Selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Running standalone Pomodoro session",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onOpenSelector,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Select Task", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun TimerRingDisplay(
    uiState: PomodoroUiState,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (uiState.totalDurationSeconds > 0) {
        uiState.timeRemainingSeconds.toFloat() / uiState.totalDurationSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "TimerProgress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val trackColor = MaterialTheme.colorScheme.surfaceVariant
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val minutes = uiState.timeRemainingSeconds / 60
            val seconds = uiState.timeRemainingSeconds % 60
            val timeString = String.format("%02d:%02d", minutes, seconds)

            // Prominent time remaining display
            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 54.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = uiState.timerState.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CycleProgressDots(
    completedCount: Int,
    targetCount: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val currentCycleStep = (completedCount % targetCount)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Cycle Progress: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 0 until targetCount) {
                val isCompletedInCycle = i < currentCycleStep
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            color = if (isCompletedInCycle) accentColor else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (isCompletedInCycle) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun ControlsRow(
    timerState: TimerState,
    accentColor: Color,
    onEvent: (PomodoroEvent) -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        // Reset Control Button (Secondary)
        IconButton(
            onClick = { onEvent(PomodoroEvent.ResetTimer) },
            enabled = timerState != TimerState.IDLE,
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (timerState != TimerState.IDLE) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset Timer",
                tint = if (timerState != TimerState.IDLE) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }

        // Primary Dynamic Action Button (START / PAUSE / RESUME)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .background(accentColor, MaterialTheme.shapes.extraLarge),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    when (timerState) {
                        TimerState.RUNNING -> onEvent(PomodoroEvent.PauseTimer)
                        TimerState.PAUSED -> onEvent(PomodoroEvent.ResumeTimer)
                        else -> onEvent(PomodoroEvent.StartTimer)
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val actionIcon = when (timerState) {
                        TimerState.RUNNING -> Icons.Default.Pause
                        else -> Icons.Default.PlayArrow
                    }
                    val actionText = when (timerState) {
                        TimerState.RUNNING -> "PAUSE"
                        TimerState.PAUSED -> "RESUME"
                        TimerState.COMPLETED -> "START NEXT"
                        TimerState.IDLE -> "START"
                    }

                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionText,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Skip Control Button (Secondary)
        IconButton(
            onClick = onSkipClick,
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Skip Session",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskSelectionBottomSheet(
    uiState: PomodoroUiState,
    onDismiss: () -> Unit,
    onSelectTask: (Task?) -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Select Focus Task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.taskSearchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search tasks...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (uiState.taskSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filtered Tasks List
            val filteredTasks = remember(uiState.availableTasks, uiState.taskSearchQuery) {
                if (uiState.taskSearchQuery.isBlank()) {
                    uiState.availableTasks
                } else {
                    uiState.availableTasks.filter { task ->
                        task.title.contains(uiState.taskSearchQuery, ignoreCase = true) ||
                                (task.description?.contains(uiState.taskSearchQuery, ignoreCase = true) == true)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Standalone / No Task Option
                item {
                    val isStandaloneSelected = uiState.selectedTask == null
                    Card(
                        onClick = { onSelectTask(null) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isStandaloneSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Standalone Session (No Task)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStandaloneSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Run timer without binding to a task",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isStandaloneSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isStandaloneSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                items(filteredTasks, key = { it.id }) { task ->
                    val isSelected = uiState.selectedTask?.id == task.id
                    Card(
                        onClick = { onSelectTask(task) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    task.priority?.let { PriorityChip(priority = it) }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val est = task.estimatedPomodoros
                                val comp = task.completedPomodoros
                                val progressStr = if (est != null) "$comp / $est Pomodoros" else "$comp Pomodoros completed"
                                Text(
                                    text = progressStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun PomodoroScreenPreview() {
    val sampleTask = Task(
        id = "task-1",
        title = "Implement Persistent Room Data Layer",
        description = "Build Task entity, DAO, Database integration, and Mappers.",
        priority = TaskPriority.HIGH,
        estimatedPomodoros = 3,
        completedPomodoros = 1
    )

    NeXTTheme {
        PomodoroScreen(
            uiState = PomodoroUiState(
                mode = PomodoroMode.FOCUS,
                timerState = TimerState.IDLE,
                timeRemainingSeconds = 1500,
                totalDurationSeconds = 1500,
                selectedTask = sampleTask,
                availableTasks = listOf(sampleTask),
                completedSessionsCount = 2
            ),
            onEvent = {},
            onNavigateToRoute = {}
        )
    }
}