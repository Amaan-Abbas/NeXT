package com.example.next.features.settings.presentation.state

import com.example.next.features.settings.domain.model.UserSettings

data class SettingsUiState(
    val isLoading: Boolean = false,
    val settings: UserSettings = UserSettings(),
    val error: String? = null,
    val isClearAllDataConfirmationShowing: Boolean = false,
    val isAutoDeleteDialogShowing: Boolean = false,
    val userMessage: String? = null
)
