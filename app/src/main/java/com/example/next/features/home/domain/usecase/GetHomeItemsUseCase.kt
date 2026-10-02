package com.example.next.features.home.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.home.domain.model.HomeItem
import com.example.next.features.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetHomeItemsUseCase(
    private val repository: HomeRepository
) {
    operator fun invoke(categoryFilter: String? = null): Flow<Result<List<HomeItem>>> {
        return repository.getHomeItems().map { result ->
            when (result) {
                is Result.Success -> {
                    val filtered = if (categoryFilter.isNullOrBlank() || categoryFilter == "All") {
                        result.data
                    } else {
                        result.data.filter { it.category.equals(categoryFilter, ignoreCase = true) }
                    }
                    Result.Success(filtered)
                }
                is Result.Error -> result
                is Result.Loading -> Result.Loading
            }
        }
    }
}
