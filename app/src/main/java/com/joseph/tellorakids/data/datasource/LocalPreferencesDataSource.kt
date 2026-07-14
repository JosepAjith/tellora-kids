package com.joseph.tellorakids.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tiny_tales_prefs")

@Singleton
class LocalPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val FAVORITE_IDS = stringSetPreferencesKey("favorite_ids")
        val RECENT_STORY_ID = stringPreferencesKey("recent_story_id")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val SELECTED_AGE_GROUP = stringPreferencesKey("selected_age_group")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
    }

    val favoriteStoryIds: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FAVORITE_IDS] ?: emptySet()
        }

    val recentStoryId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.RECENT_STORY_ID]
        }

    val isDarkMode: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DARK_MODE]
        }

    val fontSize: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] ?: 18f
        }

    val selectedAgeGroup: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_AGE_GROUP] ?: "all"
        }

    val isPremium: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_PREMIUM] ?: false
        }

    suspend fun toggleFavorite(storyId: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[PreferencesKeys.FAVORITE_IDS] ?: emptySet()
            val newFavorites = currentFavorites.toMutableSet()
            if (newFavorites.contains(storyId)) {
                newFavorites.remove(storyId)
            } else {
                newFavorites.add(storyId)
            }
            preferences[PreferencesKeys.FAVORITE_IDS] = newFavorites
        }
    }

    suspend fun saveRecentStory(storyId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECENT_STORY_ID] = storyId
        }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = isDark
        }
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = size
        }
    }

    suspend fun setSelectedAgeGroup(ageGroup: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_AGE_GROUP] = ageGroup
        }
    }

    suspend fun setPremiumStatus(isPremium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PREMIUM] = isPremium
        }
    }
}
