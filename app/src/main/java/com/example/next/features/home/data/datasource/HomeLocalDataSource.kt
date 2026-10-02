package com.example.next.features.home.data.datasource

import com.example.next.features.home.data.dto.HomeItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeLocalDataSource {

    private val _itemsFlow = MutableStateFlow<List<HomeItemDto>>(emptyList())
    val itemsFlow: Flow<List<HomeItemDto>> = _itemsFlow.asStateFlow()

    fun saveItems(items: List<HomeItemDto>) {
        _itemsFlow.value = items
    }

    fun toggleFavorite(id: String) {
        _itemsFlow.update { currentList ->
            currentList.map { item ->
                if (item.id == id) item.copy(isFavorited = !item.isFavorited) else item
            }
        }
    }

    fun getItemById(id: String): HomeItemDto? {
        return _itemsFlow.value.find { it.id == id }
    }

    fun clearAll() {
        _itemsFlow.value = emptyList()
    }
}
