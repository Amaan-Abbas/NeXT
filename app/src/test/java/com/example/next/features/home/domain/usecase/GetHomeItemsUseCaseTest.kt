package com.example.next.features.home.domain.usecase

import com.example.next.core.common.Result
import com.example.next.features.home.domain.model.HomeItem
import com.example.next.features.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetHomeItemsUseCaseTest {

    private val fakeItems = listOf(
        HomeItem("1", "Architecture Clean", "Desc 1", "Architecture", false, "Today"),
        HomeItem("2", "State Management", "Desc 2", "State", true, "Yesterday")
    )

    private val fakeRepository = object : HomeRepository {
        override fun getHomeItems(): Flow<Result<List<HomeItem>>> = flowOf(Result.Success(fakeItems))
        override fun getItemById(id: String): Flow<Result<HomeItem>> = flowOf(Result.Success(fakeItems.first()))
        override suspend fun toggleFavorite(id: String): Result<Unit> = Result.Success(Unit)
        override suspend fun refreshItems(): Result<Unit> = Result.Success(Unit)
        override suspend fun clearAll(): Result<Unit> = Result.Success(Unit)
    }

    private val useCase = GetHomeItemsUseCase(fakeRepository)

    @Test
    fun `invoke with null filter returns all items`() = runBlocking {
        val result = useCase(null).first()
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
    }

    @Test
    fun `invoke with Architecture category returns filtered items`() = runBlocking {
        val result = useCase("Architecture").first()
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(1, data.size)
        assertEquals("Architecture Clean", data.first().title)
    }
}
