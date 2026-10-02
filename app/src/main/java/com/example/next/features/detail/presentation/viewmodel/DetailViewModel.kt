package com.example.next.features.detail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.next.core.common.Result
import com.example.next.features.detail.presentation.state.DetailEvent
import com.example.next.features.detail.presentation.state.DetailUiState
import com.example.next.features.home.domain.usecase.GetItemDetailUseCase
import com.example.next.features.home.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val itemId: String,
    private val getItemDetailUseCase: GetItemDetailUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun onEvent(event: DetailEvent) {
        when (event) {
            is DetailEvent.ToggleFavorite -> toggleFavorite()
            is DetailEvent.Retry -> loadDetail()
        }
    }

    private fun loadDetail() {
        viewModelScope.launch {
            getItemDetailUseCase(itemId).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> _uiState.update { it.copy(isLoading = false, item = result.data, error = null) }
                    is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to load item detail") }
                }
            }
        }
    }

    private fun toggleFavorite() {
        val currentItem = _uiState.value.item ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(currentItem.id)
            loadDetail()
        }
    }

    class Factory(
        private val itemId: String,
        private val getItemDetailUseCase: GetItemDetailUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetailViewModel(itemId, getItemDetailUseCase, toggleFavoriteUseCase) as T
        }
    }
}
