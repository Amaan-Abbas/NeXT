package com.example.next.features.settings.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.settings.domain.model.UserSettings
import com.example.next.features.settings.domain.repository.SettingsRepository

class UpdateSettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(settings: UserSettings): Result<Unit> = repository.updateSettings(settings)
}
