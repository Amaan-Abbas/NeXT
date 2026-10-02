package com.example.next.features.home.domain.model

data class HomeItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val isFavorite: Boolean = false,
    val timestamp: String
)
