package com.joseph.tellorakids.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.joseph.tellorakids.common.utils.TextToSpeechHelper
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.usecase.GetStoryDetailsUseCase
import com.joseph.tellorakids.domain.usecase.SaveRecentStoryUseCase
import com.joseph.tellorakids.domain.usecase.ToggleFavoriteUseCase
import com.joseph.tellorakids.data.datasource.LocalPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoryUiState(
    val isLoading: Boolean = true,
    val story: Story? = null,
    val isFavorite: Boolean = false,
    val fontSize: Float = 18f,
    val isSpeaking: Boolean = false,
    val autoPlayEnabled: Boolean = false,
    val isNightMode: Boolean = false, // New: Warm background for night reading
    val readingProgress: Float = 0f, // New: Scroll progress percentage
    val error: String? = null
)

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val getStoryDetailsUseCase: GetStoryDetailsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val saveRecentStoryUseCase: SaveRecentStoryUseCase,
    private val preferencesDataSource: LocalPreferencesDataSource,
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _speechFinishedEvent = MutableSharedFlow<Unit>()
    val speechFinishedEvent = _speechFinishedEvent.asSharedFlow()

    private val ttsHelper = TextToSpeechHelper(application) {
        viewModelScope.launch { _speechFinishedEvent.emit(Unit) }
    }
    
    private val storyId: String = checkNotNull(savedStateHandle["storyId"])

    private val _uiState = MutableStateFlow(StoryUiState())
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()

    init {
        loadStoryDetails()
        saveAsRecent()
        observeSettings()
        observeTts()
    }

    fun retry() {
        loadStoryDetails()
    }

    private fun loadStoryDetails() {
        viewModelScope.launch {
            getStoryDetailsUseCase(storyId)
                .combine(preferencesDataSource.getStoryProgress(storyId)) { details, progress ->
                    details to progress
                }
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { (details, progress) ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            story = details?.story,
                            isFavorite = details?.isFavorite ?: false,
                            readingProgress = if (details?.story?.pages?.isNotEmpty() == true) {
                                (progress.toFloat() / details.story.pages.size)
                            } else 0f
                        )
                    }
                }
        }
    }

    fun updateCurrentPage(page: Int) {
        viewModelScope.launch {
            preferencesDataSource.saveStoryProgress(storyId, page)
            _uiState.update { state ->
                val totalPages = state.story?.pages?.size ?: 1
                state.copy(readingProgress = page.toFloat() / totalPages)
            }
        }
    }

    private fun saveAsRecent() {
        viewModelScope.launch {
            saveRecentStoryUseCase(storyId)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            preferencesDataSource.fontSize.collect { size ->
                _uiState.update { it.copy(fontSize = size) }
            }
        }
    }

    private fun observeTts() {
        viewModelScope.launch {
            ttsHelper.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
    }

    fun toggleSpeak(text: String) {
        if (_uiState.value.isSpeaking) {
            ttsHelper.stop()
            _uiState.update { it.copy(autoPlayEnabled = false) }
        } else {
            _uiState.update { it.copy(autoPlayEnabled = true) }
            ttsHelper.speak(text)
        }
    }
    
    fun stopSpeak() {
        ttsHelper.stop()
        _uiState.update { it.copy(autoPlayEnabled = false) }
    }

    fun toggleAutoPlay() {
        _uiState.update { it.copy(autoPlayEnabled = !it.autoPlayEnabled) }
        if (!_uiState.value.autoPlayEnabled) {
            ttsHelper.stop()
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            toggleFavoriteUseCase(storyId)
        }
    }

    fun updateFontSize(newSize: Float) {
        viewModelScope.launch {
            preferencesDataSource.setFontSize(newSize)
        }
    }

    fun toggleNightMode() {
        _uiState.update { it.copy(isNightMode = !it.isNightMode) }
    }

    fun updateReadingProgress(progress: Float) {
        _uiState.update { it.copy(readingProgress = progress) }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}
