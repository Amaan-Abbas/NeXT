package com.example.next.features.settings.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.settings.domain.model.UserSettings
import com.example.next.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<Result<UserSettings>> = repository.getSettings()
}
