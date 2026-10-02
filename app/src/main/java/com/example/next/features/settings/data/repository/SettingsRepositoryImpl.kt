package com.example.next.features.settings.data.repository

import com.example.next.core.common.Result
import com.example.next.features.settings.domain.model.UserSettings
import com.example.next.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl : SettingsRepository {

    private val _settingsFlow = MutableStateFlow(UserSettings())

    override fun getSettings(): Flow<Result<UserSettings>> {
        return _settingsFlow.asStateFlow().map { Result.Success(it) }
    }

    override suspend fun updateSettings(settings: UserSettings): Result<Unit> {
        _settingsFlow.value = settings
        return Result.Success(Unit)
    }
}
