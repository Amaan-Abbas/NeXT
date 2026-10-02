package com.example.next.features.detail.presentation.state

sealed interface DetailEvent {
    data object ToggleFavorite : DetailEvent
    data object Retry : DetailEvent
}
