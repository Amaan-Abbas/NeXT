package com.example.next.features.home.data.dto

data class HomeItemDto(
    val id: String,
    val title: String,
    val snippet: String,
    val categoryName: String,
    val isFavorited: Boolean,
    val formattedDate: String
)
