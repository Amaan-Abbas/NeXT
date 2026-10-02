package com.example.next.features.home.presentation.state

import com.example.next.features.home.domain.model.HomeItem

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val items: List<HomeItem> = emptyList(),
    val selectedCategory: String = "All",
    val categories: List<String> = listOf("All", "Architecture", "State", "Design"),
    val error: String? = null
)
