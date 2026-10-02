package com.example.next.features.home.domain.repository

import com.example.next.core.common.Result
import com.example.next.features.home.domain.model.HomeItem
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getHomeItems(): Flow<Result<List<HomeItem>>>
    fun getItemById(id: String): Flow<Result<HomeItem>>
    suspend fun toggleFavorite(id: String): Result<Unit>
    suspend fun refreshItems(): Result<Unit>
    suspend fun clearAll(): Result<Unit>
}
