package com.example.next.features.home.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.home.domain.model.HomeItem
import com.example.next.features.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class GetItemDetailUseCase(
    private val repository: HomeRepository
) {
    operator fun invoke(itemId: String): Flow<Result<HomeItem>> {
        return repository.getItemById(itemId)
    }
}
