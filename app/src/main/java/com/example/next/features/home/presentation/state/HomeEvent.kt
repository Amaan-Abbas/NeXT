package com.example.next.features.home.presentation.state

sealed interface HomeEvent {
    data object Refresh : HomeEvent
    data object Retry : HomeEvent
    data class SelectCategory(val category: String) : HomeEvent
    data class ToggleFavorite(val id: String) : HomeEvent
    data class ItemClicked(val id: String) : HomeEvent
}
