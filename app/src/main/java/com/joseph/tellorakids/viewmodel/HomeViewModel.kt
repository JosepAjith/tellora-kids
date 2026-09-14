package com.joseph.tellorakids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseph.tellorakids.domain.model.AgeGroup
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.repository.StoryRepository
import com.joseph.tellorakids.domain.usecase.GetHomeStoriesUseCase
import com.joseph.tellorakids.domain.usecase.ToggleFavoriteUseCase
import com.joseph.tellorakids.common.utils.AdsManager
import com.joseph.tellorakids.common.managers.ReviewManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredStories: List<Story> = emptyList(),
    val newStories: List<Story> = emptyList(),
    val recentStory: Story? = null,
    val recentStoryProgress: Int = 1,
    val categories: List<Story> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val availableAgeGroups: List<String> = emptyList(),
    val selectedAgeGroup: String = "all",
    val isPremium: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeStoriesUseCase: GetHomeStoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val repository: StoryRepository,
    val adsManager: AdsManager,
    val reviewManager: ReviewManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
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
                    repository.getFavoriteStoryIds(),
                    repository.getAllStories("all") // To extract all age groups
                ) { homeStories, favorites, allStories ->
                    val ageGroups = allStories.map { it.ageGroup }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                    
                    Quadruple(homeStories, favorites, ageGroup, ageGroups)
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { (homeStories, favorites, ageGroup, ageGroups) ->
                val recentId = homeStories.recent?.id
                val progress = if (recentId != null) {
                    repository.getStoryProgress(recentId).first()
                } else 1

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        featuredStories = homeStories.featured,
                        newStories = homeStories.allStories,
                        recentStory = homeStories.recent,
                        recentStoryProgress = progress,
                        categories = homeStories.categories,
                        favoriteIds = favorites,
                        availableAgeGroups = ageGroups,
                        selectedAgeGroup = ageGroup
                    )
                }
            }
        }
    }

    private data class Quadruple<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )

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
