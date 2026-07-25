package com.joseph.tellorakids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.repository.StoryRepository
import com.joseph.tellorakids.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryStory(
    val story: Story,
    val currentPage: Int,
    val isFavorite: Boolean
)

data class LibraryUiState(
    val isLoading: Boolean = true,
    val continueReading: List<LibraryStory> = emptyList(),
    val favorites: List<LibraryStory> = emptyList(),
    val completed: List<LibraryStory> = emptyList(),
    val isPremium: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: StoryRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibraryData()
    }

    fun retry() {
        loadLibraryData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadLibraryData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                repository.getAllStories(),
                repository.getFavoriteStoryIds(),
                repository.isPremiumUser()
            ) { allStories, favoriteIds, isPremium ->
                Triple(allStories, favoriteIds, isPremium)
            }.flatMapLatest { (allStories, favoriteIds, isPremium) ->
                _uiState.update { it.copy(isPremium = isPremium) }
                // Create a flow that combines progress for all relevant stories
                val storyFlows = allStories.map { story ->
                    repository.getStoryProgress(story.id).map { progress ->
                        LibraryStory(story, progress, favoriteIds.contains(story.id))
                    }
                }
                
                if (storyFlows.isEmpty()) {
                    flowOf(emptyList<LibraryStory>())
                } else {
                    combine(storyFlows) { it.toList() }
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { allLibraryStories ->
                val continueReading = allLibraryStories.filter { 
                    it.currentPage > 1 && it.currentPage < (it.story.pages.size.takeIf { s -> s > 0 } ?: 1) 
                }
                val favorites = allLibraryStories.filter { it.isFavorite }
                val completed = allLibraryStories.filter { 
                    it.currentPage >= (it.story.pages.size.takeIf { s -> s > 0 } ?: 1) 
                }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        continueReading = continueReading,
                        favorites = favorites,
                        completed = completed
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
