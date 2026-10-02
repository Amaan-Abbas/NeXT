package com.example.next.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.next.core.common.Result
import com.example.next.features.home.domain.usecase.GetHomeItemsUseCase
import com.example.next.features.home.domain.usecase.ToggleFavoriteUseCase
import com.example.next.features.home.presentation.state.HomeEvent
import com.example.next.features.home.presentation.state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getHomeItemsUseCase: GetHomeItemsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.Refresh -> refresh()
            is HomeEvent.Retry -> loadItems()
            is HomeEvent.SelectCategory -> selectCategory(event.category)
            is HomeEvent.ToggleFavorite -> toggleFavorite(event.id)
            is HomeEvent.ItemClicked -> { /* Navigation handled in UI listener */ }
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            getHomeItemsUseCase(_uiState.value.selectedCategory).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                items = result.data,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = result.message ?: "Failed to load items"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadItems()
    }

    private fun toggleFavorite(id: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(id)
        }
    }

    private fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadItems()
    }

    class Factory(
        private val getHomeItemsUseCase: GetHomeItemsUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(getHomeItemsUseCase, toggleFavoriteUseCase) as T
        }
    }
}
