package com.example.next.features.settings.domain.model

enum class AutoDeleteUnit(val label: String) {
    DISABLED("Disabled"),
    DAYS("Days"),
    WEEKS("Weeks"),
    MONTHS("Months")
}

data class UserSettings(
    val isDarkMode: Boolean = false,
    val isDynamicColor: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val autoDeleteUnit: AutoDeleteUnit = AutoDeleteUnit.DISABLED,
    val autoDeleteValue: Int = 30
)
