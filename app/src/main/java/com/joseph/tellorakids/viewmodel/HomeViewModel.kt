package com.joseph.tellorakids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseph.tellorakids.domain.model.AgeGroup
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.repository.StoryRepository
import com.joseph.tellorakids.domain.usecase.GetHomeStoriesUseCase
import com.joseph.tellorakids.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredStories: List<Story> = emptyList(),
    val recentStory: Story? = null,
    val categories: List<Story> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val selectedAgeGroup: String = "all",
    val isPremium: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeStoriesUseCase: GetHomeStoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val repository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            // Combine age group and premium status with home data
            combine(
                repository.getSelectedAgeGroup(),
                repository.isPremiumUser()
            ) { ageGroup, isPremium ->
                ageGroup to isPremium
            }.flatMapLatest { (ageGroup, isPremium) ->
                _uiState.update { it.copy(selectedAgeGroup = ageGroup, isPremium = isPremium) }
                combine(
                    getHomeStoriesUseCase(ageGroup),
                    repository.getFavoriteStoryIds()
                ) { homeStories, favorites ->
                    Triple(homeStories, favorites, ageGroup)
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { (homeStories, favorites, ageGroup) ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        featuredStories = homeStories.featured,
                        recentStory = homeStories.recent,
                        categories = homeStories.categories,
                        favoriteIds = favorites,
                        selectedAgeGroup = ageGroup
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

    fun setAgeGroup(ageGroup: String) {
        viewModelScope.launch {
            repository.setSelectedAgeGroup(ageGroup)
        }
    }
}
