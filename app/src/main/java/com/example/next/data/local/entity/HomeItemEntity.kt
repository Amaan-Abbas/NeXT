package com.example.next.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.next.features.home.domain.model.HomeItem

@Entity(tableName = "home_items")
data class HomeItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val isFavorite: Boolean,
    val timestamp: String
)

fun HomeItemEntity.toDomain(): HomeItem = HomeItem(
    id = id,
    title = title,
    description = description,
    category = category,
    isFavorite = isFavorite,
    timestamp = timestamp
)

fun HomeItem.toEntity(): HomeItemEntity = HomeItemEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    isFavorite = isFavorite,
    timestamp = timestamp
)
