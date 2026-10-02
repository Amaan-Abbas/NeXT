package com.example.next.features.settings.presentation.state

import com.example.next.features.settings.domain.model.AutoDeleteUnit

sealed interface SettingsEvent {
    data class ToggleDarkMode(val enabled: Boolean) : SettingsEvent
    data class ToggleDynamicColor(val enabled: Boolean) : SettingsEvent
    data class ToggleNotifications(val enabled: Boolean) : SettingsEvent

    data object ShowClearAllDataConfirmation : SettingsEvent
    data object DismissClearAllDataConfirmation : SettingsEvent
    data object ConfirmClearAllData : SettingsEvent

    data object ShowAutoDeleteDialog : SettingsEvent
    data object DismissAutoDeleteDialog : SettingsEvent
    data class UpdateAutoDeleteSettings(val unit: AutoDeleteUnit, val value: Int) : SettingsEvent
    data object TriggerAutoDeleteNow : SettingsEvent
    data object DismissUserMessage : SettingsEvent
}
