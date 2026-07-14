package com.joseph.tellorakids.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.usecase.GetStoriesByCategoryUseCase
import com.joseph.tellorakids.domain.usecase.ToggleFavoriteUseCase
import com.joseph.tellorakids.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = true,
    val categoryName: String = "",
    val stories: List<Story> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isPremium: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getStoriesByCategoryUseCase: GetStoriesByCategoryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val repository: StoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryName: String = checkNotNull(savedStateHandle["categoryName"])

    private val _uiState = MutableStateFlow(CategoryUiState(categoryName = categoryName))
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategoryStories()
    }

    private fun loadCategoryStories() {
        viewModelScope.launch {
            repository.getSelectedAgeGroup().flatMapLatest { ageGroup ->
                combine(
                    getStoriesByCategoryUseCase(categoryName, ageGroup),
                    repository.getFavoriteStoryIds(),
                    repository.isPremiumUser()
                ) { stories, favorites, isPremium ->
                    Triple(stories, favorites, isPremium)
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { (stories, favorites, isPremium) ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        stories = stories,
                        favoriteIds = favorites,
                        isPremium = isPremium
                    )
                }
            }
        }
    }

    fun toggleFavorite(storyId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(storyId)
        }
    }
}
