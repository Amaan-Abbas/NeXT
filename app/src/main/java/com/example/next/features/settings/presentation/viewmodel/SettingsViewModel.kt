package com.example.next.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.next.core.common.Result
import com.example.next.features.settings.domain.model.AutoDeleteUnit
import com.example.next.features.settings.domain.model.UserSettings
import com.example.next.features.settings.domain.usecase.AutoDeleteOldDataUseCase
import com.example.next.features.settings.domain.usecase.ClearAllAppDataUseCase
import com.example.next.features.settings.domain.usecase.GetSettingsUseCase
import com.example.next.features.settings.domain.usecase.UpdateSettingsUseCase
import com.example.next.features.settings.presentation.state.SettingsEvent
import com.example.next.features.settings.presentation.state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val clearAllAppDataUseCase: ClearAllAppDataUseCase,
    private val autoDeleteOldDataUseCase: AutoDeleteOldDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun onEvent(event: SettingsEvent) {
        val currentSettings = _uiState.value.settings
        when (event) {
            is SettingsEvent.ToggleDarkMode -> {
                updateUserSettings(currentSettings.copy(isDarkMode = event.enabled))
            }
            is SettingsEvent.ToggleDynamicColor -> {
                updateUserSettings(currentSettings.copy(isDynamicColor = event.enabled))
            }
            is SettingsEvent.ToggleNotifications -> {
                updateUserSettings(currentSettings.copy(isNotificationsEnabled = event.enabled))
            }
            is SettingsEvent.ShowClearAllDataConfirmation -> {
                _uiState.update { it.copy(isClearAllDataConfirmationShowing = true) }
            }
            is SettingsEvent.DismissClearAllDataConfirmation -> {
                _uiState.update { it.copy(isClearAllDataConfirmationShowing = false) }
            }
            is SettingsEvent.ConfirmClearAllData -> {
                clearAllAppData()
            }
            is SettingsEvent.ShowAutoDeleteDialog -> {
                _uiState.update { it.copy(isAutoDeleteDialogShowing = true) }
            }
            is SettingsEvent.DismissAutoDeleteDialog -> {
                _uiState.update { it.copy(isAutoDeleteDialogShowing = false) }
            }
            is SettingsEvent.UpdateAutoDeleteSettings -> {
                val updated = currentSettings.copy(
                    autoDeleteUnit = event.unit,
                    autoDeleteValue = event.value
                )
                _uiState.update { it.copy(isAutoDeleteDialogShowing = false) }
                updateUserSettings(updated)
                runAutoDelete(event.unit, event.value)
            }
            is SettingsEvent.TriggerAutoDeleteNow -> {
                runAutoDelete(currentSettings.autoDeleteUnit, currentSettings.autoDeleteValue)
            }
            is SettingsEvent.DismissUserMessage -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }

    private fun updateUserSettings(newSettings: UserSettings) {
        viewModelScope.launch {
            updateSettingsUseCase(newSettings)
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            getSettingsUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Result.Success -> {
                        _uiState.update { it.copy(isLoading = false, settings = result.data) }
                        if (result.data.autoDeleteUnit != AutoDeleteUnit.DISABLED) {
                            runAutoDelete(result.data.autoDeleteUnit, result.data.autoDeleteValue)
                        }
                    }
                    is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    private fun clearAllAppData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearAllDataConfirmationShowing = false) }
            val result = clearAllAppDataUseCase()
            if (result is Result.Success) {
                _uiState.update { it.copy(userMessage = "All application data deleted successfully.") }
            } else if (result is Result.Error) {
                _uiState.update { it.copy(error = result.message ?: "Failed to delete application data.") }
            }
        }
    }

    private fun runAutoDelete(unit: AutoDeleteUnit, value: Int) {
        if (unit == AutoDeleteUnit.DISABLED) return
        viewModelScope.launch {
            val result = autoDeleteOldDataUseCase(unit, value)
            if (result is Result.Success) {
                _uiState.update { it.copy(userMessage = "Auto-delete check complete.") }
            }
        }
    }

    class Factory(
        private val getSettingsUseCase: GetSettingsUseCase,
        private val updateSettingsUseCase: UpdateSettingsUseCase,
        private val clearAllAppDataUseCase: ClearAllAppDataUseCase,
        private val autoDeleteOldDataUseCase: AutoDeleteOldDataUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                getSettingsUseCase = getSettingsUseCase,
                updateSettingsUseCase = updateSettingsUseCase,
                clearAllAppDataUseCase = clearAllAppDataUseCase,
                autoDeleteOldDataUseCase = autoDeleteOldDataUseCase
            ) as T
        }
    }
}
