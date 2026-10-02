package com.example.next.features.home.data.datasource

import com.example.next.features.home.data.dto.HomeItemDto
import kotlinx.coroutines.delay

class HomeRemoteDataSource {

    private val initialRemoteData = listOf(
        HomeItemDto(
            id = "1",
            title = "Jetpack Compose Architecture",
            snippet = "Learn how to build scalable, reactive UI screens using Material 3 and MVVM.",
            categoryName = "Architecture",
            isFavorited = true,
            formattedDate = "Today"
        ),
        HomeItemDto(
            id = "2",
            title = "Unidirectional Data Flow",
            snippet = "Discover state management patterns using StateFlow and immutable UiState.",
            categoryName = "State",
            isFavorited = false,
            formattedDate = "Yesterday"
        ),
        HomeItemDto(
            id = "3",
            title = "Clean Architecture Boundaries",
            snippet = "Enforce strict dependency direction: UI -> ViewModel -> Domain -> Repository.",
            categoryName = "Architecture",
            isFavorited = false,
            formattedDate = "3 days ago"
        ),
        HomeItemDto(
            id = "4",
            title = "Material 3 Expressive UI",
            snippet = "Utilize semantic colors, dynamic themes, and rounded shapes.",
            categoryName = "Design",
            isFavorited = true,
            formattedDate = "1 week ago"
        )
    )

    suspend fun fetchHomeItems(): List<HomeItemDto> {
        delay(600) // Simulate network delay
        return initialRemoteData
    }
}
