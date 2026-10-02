package com.example.next.features.detail.presentation.state

import com.example.next.features.home.domain.model.HomeItem

data class DetailUiState(
    val isLoading: Boolean = false,
    val item: HomeItem? = null,
    val error: String? = null
)
