package com.joseph.tellorakids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.usecase.SearchStoriesUseCase
import com.joseph.tellorakids.domain.usecase.ToggleFavoriteUseCase
import com.joseph.tellorakids.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Story> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isPremium: Boolean = false,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchStoriesUseCase: SearchStoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val repository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getFavoriteStoryIds(),
                repository.isPremiumUser()
            ) { favorites, isPremium ->
                _uiState.update { it.copy(favoriteIds = favorites, isPremium = isPremium) }
            }.collect()
        }
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery, isLoading = true) }
        
        viewModelScope.launch {
            if (newQuery.isBlank()) {
                _uiState.update { it.copy(results = emptyList(), isLoading = false) }
            } else {
                repository.getSelectedAgeGroup().flatMapLatest { ageGroup ->
                    searchStoriesUseCase(newQuery, ageGroup)
                }.collect { results ->
                    _uiState.update { it.copy(results = results, isLoading = false) }
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
