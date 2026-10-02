package com.example.next.features.settings.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.next.core.ui.components.AppNavBar
import com.example.next.core.ui.components.AppTopBar
import com.example.next.core.ui.components.ConfirmationDialog
import com.example.next.features.settings.domain.model.AutoDeleteUnit
import com.example.next.features.settings.domain.model.UserSettings
import com.example.next.features.settings.presentation.state.SettingsEvent
import com.example.next.features.settings.presentation.state.SettingsUiState
import com.example.next.ui.theme.NeXTTheme

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onEvent(SettingsEvent.DismissUserMessage)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(title = "App Settings")
        },
        bottomBar = {
            AppNavBar(
                currentRoute = "settings",
                onNavigateToRoute = onNavigateToRoute
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Preferences
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SettingSwitchRow(
                        title = "Dark Theme",
                        description = "Enable modern Material 3 dark color scheme",
                        checked = uiState.settings.isDarkMode,
                        onCheckedChange = { onEvent(SettingsEvent.ToggleDarkMode(it)) }
                    )
                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingSwitchRow(
                        title = "Dynamic Color",
                        description = "Use system wallpaper colors (Android 12+)",
                        checked = uiState.settings.isDynamicColor,
                        onCheckedChange = { onEvent(SettingsEvent.ToggleDynamicColor(it)) }
                    )
                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingSwitchRow(
                        title = "Notifications",
                        description = "Receive updates and architectural recommendations",
                        checked = uiState.settings.isNotificationsEnabled,
                        onCheckedChange = { onEvent(SettingsEvent.ToggleNotifications(it)) }
                    )
                }
            }

            // Data & Storage Management
            Text(
                text = "Data & Storage Management",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val autoDeleteStatusText = if (uiState.settings.autoDeleteUnit == AutoDeleteUnit.DISABLED) {
                        "Disabled"
                    } else {
                        "${uiState.settings.autoDeleteValue} ${uiState.settings.autoDeleteUnit.label}"
                    }

                    SettingActionRow(
                        title = "Auto-Delete Past Data",
                        description = "Automatically delete data older than past $autoDeleteStatusText",
                        icon = Icons.Default.AutoDelete,
                        onClick = { onEvent(SettingsEvent.ShowAutoDeleteDialog) }
                    )
                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingActionRow(
                        title = "Clear All Application Data",
                        description = "Permanently delete all tasks, pomodoro sessions, and local data",
                        icon = Icons.Default.DeleteForever,
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { onEvent(SettingsEvent.ShowClearAllDataConfirmation) }
                    )
                }
            }
        }
    }

    if (uiState.isClearAllDataConfirmationShowing) {
        ConfirmationDialog(
            title = "Clear All Application Data",
            message = "Are you sure you want to permanently delete all tasks, pomodoro sessions, and local data? This action cannot be undone.",
            onConfirm = { onEvent(SettingsEvent.ConfirmClearAllData) },
            onDismiss = { onEvent(SettingsEvent.DismissClearAllDataConfirmation) },
            confirmText = "Delete All Data",
            dismissText = "Cancel"
        )
    }

    if (uiState.isAutoDeleteDialogShowing) {
        AutoDeleteConfigDialog(
            currentUnit = uiState.settings.autoDeleteUnit,
            currentValue = uiState.settings.autoDeleteValue,
            onDismiss = { onEvent(SettingsEvent.DismissAutoDeleteDialog) },
            onSave = { unit, value -> onEvent(SettingsEvent.UpdateAutoDeleteSettings(unit, value)) }
        )
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingActionRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == MaterialTheme.colorScheme.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AutoDeleteConfigDialog(
    currentUnit: AutoDeleteUnit,
    currentValue: Int,
    onDismiss: () -> Unit,
    onSave: (AutoDeleteUnit, Int) -> Unit
) {
    var selectedUnit by remember { mutableStateOf(currentUnit) }
    var valueInput by remember { mutableStateOf(if (currentValue <= 0) "30" else currentValue.toString()) }
    var valueError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Auto-Delete Data Settings") },
        text = {
            Column {
                Text(
                    text = "Select period unit:",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AutoDeleteUnit.entries.size) { index ->
                        val unit = AutoDeleteUnit.entries[index]
                        FilterChip(
                            selected = selectedUnit == unit,
                            onClick = { selectedUnit = unit },
                            label = { Text(unit.label) }
                        )
                    }
                }

                if (selectedUnit != AutoDeleteUnit.DISABLED) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = {
                            valueInput = it.filter { c -> c.isDigit() }
                            if (valueInput.isNotBlank()) valueError = null
                        },
                        label = { Text("Number of ${selectedUnit.label} (n)") },
                        isError = valueError != null,
                        supportingText = valueError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedUnit == AutoDeleteUnit.DISABLED) {
                        onSave(AutoDeleteUnit.DISABLED, 0)
                        return@TextButton
                    }
                    val num = valueInput.toIntOrNull()
                    if (num == null || num <= 0) {
                        valueError = "Enter a valid positive number"
                        return@TextButton
                    }
                    onSave(selectedUnit, num)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun SettingsScreenPreview() {
    NeXTTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                settings = UserSettings(
                    isDarkMode = true,
                    isDynamicColor = true,
                    isNotificationsEnabled = true,
                    autoDeleteUnit = AutoDeleteUnit.DAYS,
                    autoDeleteValue = 30
                )
            ),
            onEvent = {},
            onNavigateToRoute = {}
        )
    }
}

