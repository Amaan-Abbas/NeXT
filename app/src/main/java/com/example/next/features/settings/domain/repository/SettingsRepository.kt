package com.example.next.features.settings.domain.repository

import com.example.next.core.common.Result
import com.example.next.features.settings.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<Result<UserSettings>>
    suspend fun updateSettings(settings: UserSettings): Result<Unit>
}
