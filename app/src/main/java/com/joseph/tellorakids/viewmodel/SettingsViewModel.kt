package com.joseph.tellorakids.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseph.tellorakids.data.datasource.LocalPreferencesDataSource
import com.joseph.tellorakids.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val fontSize: Float = 18f,
    val isPremium: Boolean = false,
    val appVersion: String = "1.0.0"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataSource: LocalPreferencesDataSource,
    private val repository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesDataSource.isDarkMode,
                preferencesDataSource.fontSize,
                repository.isPremiumUser()
            ) { isDark, size, isPremium ->
                SettingsUiState(
                    isDarkMode = isDark ?: false,
                    fontSize = size,
                    isPremium = isPremium
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.setDarkMode(enabled)
        }
    }

    fun updateFontSize(size: Float) {
        viewModelScope.launch {
            preferencesDataSource.setFontSize(size)
        }
    }
}
