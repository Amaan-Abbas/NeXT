package com.example.next.features.home.data.mapper

import com.example.next.features.home.data.dto.HomeItemDto
import com.example.next.features.home.domain.model.HomeItem

fun HomeItemDto.toDomain(): HomeItem {
    return HomeItem(
        id = id,
        title = title,
        description = snippet,
        category = categoryName,
        isFavorite = isFavorited,
        timestamp = formattedDate
    )
}

fun HomeItem.toDto(): HomeItemDto {
    return HomeItemDto(
        id = id,
        title = title,
        snippet = description,
        categoryName = category,
        isFavorited = isFavorite,
        formattedDate = timestamp
    )
}
