package com.example.next.features.home.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.home.domain.repository.HomeRepository

class ToggleFavoriteUseCase(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.toggleFavorite(id)
    }
}
