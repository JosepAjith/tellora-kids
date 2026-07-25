package com.joseph.tellorakids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.usecase.GetFavoriteStoriesUseCase
import com.joseph.tellorakids.domain.usecase.ToggleFavoriteUseCase
import com.joseph.tellorakids.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val favoriteStories: List<Story> = emptyList(),
    val isPremium: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoriteStoriesUseCase: GetFavoriteStoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val repository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            combine(
                getFavoriteStoriesUseCase(),
                repository.isPremiumUser()
            ) { stories, isPremium ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        favoriteStories = stories,
                        isPremium = isPremium
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect()
        }
    }

    fun toggleFavorite(storyId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(storyId)
        }
    }
}
