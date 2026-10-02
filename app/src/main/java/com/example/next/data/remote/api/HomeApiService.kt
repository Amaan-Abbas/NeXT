package com.example.next.data.remote.api

import com.example.next.features.home.data.dto.HomeItemDto

interface HomeApiService {
    suspend fun getHomeItems(): List<HomeItemDto>
    suspend fun getHomeItemById(id: String): HomeItemDto
}
