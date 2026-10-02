package com.example.next.features.home.data.repository

import com.example.next.core.common.DispatcherProvider
import com.example.next.core.common.Result
import com.example.next.features.home.data.datasource.HomeLocalDataSource
import com.example.next.features.home.data.datasource.HomeRemoteDataSource
import com.example.next.features.home.data.mapper.toDomain
import com.example.next.features.home.domain.model.HomeItem
import com.example.next.features.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HomeRepositoryImpl(
    private val localDataSource: HomeLocalDataSource,
    private val remoteDataSource: HomeRemoteDataSource,
    private val dispatchers: DispatcherProvider
) : HomeRepository {

    override fun getHomeItems(): Flow<Result<List<HomeItem>>> = flow {
        emit(Result.Loading)
        try {
            val remoteData = remoteDataSource.fetchHomeItems()
            localDataSource.saveItems(remoteData)
        } catch (e: Exception) {
            // Log or handle initial fetch failure gracefully
        }

        // Stream reactive updates from local data source
        val localFlow = localDataSource.itemsFlow.map { dtoList ->
            Result.Success(dtoList.map { it.toDomain() })
        }
        emitAll(localFlow)
    }.flowOn(dispatchers.io)

    override fun getItemById(id: String): Flow<Result<HomeItem>> = flow {
        emit(Result.Loading)
        val localItem = localDataSource.getItemById(id)
        if (localItem != null) {
            emit(Result.Success(localItem.toDomain()))
        } else {
            emit(Result.Error(message = "Item not found"))
        }
    }.flowOn(dispatchers.io)

    override suspend fun toggleFavorite(id: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.toggleFavorite(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun refreshItems(): Result<Unit> = withContext(dispatchers.io) {
        try {
            val remoteItems = remoteDataSource.fetchHomeItems()
            localDataSource.saveItems(remoteItems)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun clearAll(): Result<Unit> = withContext(dispatchers.io) {
        try {
            localDataSource.clearAll()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }
}
