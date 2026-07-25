package com.joseph.tellorakids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.usecase.SearchStoriesUseCase
import com.joseph.tellorakids.domain.usecase.ToggleFavoriteUseCase
import com.joseph.tellorakids.domain.repository.StoryRepository
import com.joseph.tellorakids.domain.model.StoryCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Story> = emptyList(),
    val categories: List<String> = StoryCategory.entries.map { it.displayName },
    val selectedCategory: String? = null,
    val availableAgeGroups: List<String> = emptyList(),
    val selectedAgeGroup: String = "all",
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

    fun retry() {
        onQueryChanged(_uiState.value.query)
    }

    init {
        viewModelScope.launch {
            combine(
                repository.getFavoriteStoryIds(),
                repository.isPremiumUser(),
                repository.getAllStories("all")
            ) { favorites, isPremium, allStories ->
                val ageGroups = allStories.map { it.ageGroup }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                
                _uiState.update { 
                    it.copy(
                        favoriteIds = favorites, 
                        isPremium = isPremium,
                        availableAgeGroups = ageGroups
                    ) 
                }
            }.collect()
        }
        // Load initial stories
        search()
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        search()
    }

    fun onAgeSelected(age: String) {
        _uiState.update { it.copy(selectedAgeGroup = age) }
        search()
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        search()
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    private fun search() {
        searchJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }
        
        searchJob = viewModelScope.launch {
            val query = _uiState.value.query
            val category = _uiState.value.selectedCategory
            val ageGroup = _uiState.value.selectedAgeGroup
            
            if (query.isBlank() && category == null) {
                // Initial load or filtered by age only
                repository.getAllStories(ageGroup).collect { results ->
                    _uiState.update { it.copy(results = results, isLoading = false) }
                }
            } else {
                // Search with query and/or category within the selected age group
                searchStoriesUseCase(query, ageGroup).map { stories ->
                    if (category != null) {
                        stories.filter { it.category == category }
                    } else {
                        stories
                    }
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
