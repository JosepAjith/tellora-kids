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
        
        // Ads Management
        val LAST_AD_TIME = longPreferencesKey("last_ad_time")
        val DAILY_AD_COUNT = intPreferencesKey("daily_ad_count")
        val LAST_AD_DATE = stringPreferencesKey("last_ad_date")

        // Review Management
        val APP_OPEN_COUNT = intPreferencesKey("app_open_count")
        val STORIES_READ_COUNT = intPreferencesKey("stories_read_count")
        val FIRST_INSTALL_DATE = longPreferencesKey("first_install_date")
        val LAST_REVIEW_DATE = longPreferencesKey("last_review_date")
        val UNIQUE_DAYS_USED = stringSetPreferencesKey("unique_days_used")

        fun storyProgressKey(storyId: String) = intPreferencesKey("story_progress_$storyId")
    }

    // Ads Data
    val lastAdTime: Flow<Long> = context.dataStore.data.map { it[PreferencesKeys.LAST_AD_TIME] ?: 0L }
    val dailyAdCount: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.DAILY_AD_COUNT] ?: 0 }
    val lastAdDate: Flow<String?> = context.dataStore.data.map { it[PreferencesKeys.LAST_AD_DATE] }

    // Review Data
    val appOpenCount: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.APP_OPEN_COUNT] ?: 0 }
    val storiesReadCount: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.STORIES_READ_COUNT] ?: 0 }
    val firstInstallDate: Flow<Long> = context.dataStore.data.map { it[PreferencesKeys.FIRST_INSTALL_DATE] ?: 0L }
    val lastReviewDate: Flow<Long> = context.dataStore.data.map { it[PreferencesKeys.LAST_REVIEW_DATE] ?: 0L }
    val uniqueDaysUsed: Flow<Set<String>> = context.dataStore.data.map { it[PreferencesKeys.UNIQUE_DAYS_USED] ?: emptySet() }

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

    fun getStoryProgress(storyId: String): Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.storyProgressKey(storyId)] ?: 1
        }

    suspend fun saveStoryProgress(storyId: String, page: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.storyProgressKey(storyId)] = page
        }
    }

    suspend fun incrementAppOpenCount() {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.APP_OPEN_COUNT] ?: 0
            prefs[PreferencesKeys.APP_OPEN_COUNT] = current + 1
            
            // Track unique days
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            val days = prefs[PreferencesKeys.UNIQUE_DAYS_USED] ?: emptySet()
            prefs[PreferencesKeys.UNIQUE_DAYS_USED] = days + today

            // Set install date if first time
            if (prefs[PreferencesKeys.FIRST_INSTALL_DATE] == null) {
                prefs[PreferencesKeys.FIRST_INSTALL_DATE] = System.currentTimeMillis()
            }
        }
    }

    suspend fun incrementStoriesReadCount() {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.STORIES_READ_COUNT] ?: 0
            prefs[PreferencesKeys.STORIES_READ_COUNT] = current + 1
        }
    }

    suspend fun recordAdShown() {
        context.dataStore.edit { prefs ->
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            val lastDate = prefs[PreferencesKeys.LAST_AD_DATE]
            
            val count = if (lastDate == today) {
                (prefs[PreferencesKeys.DAILY_AD_COUNT] ?: 0) + 1
            } else {
                1
            }
            
            prefs[PreferencesKeys.DAILY_AD_COUNT] = count
            prefs[PreferencesKeys.LAST_AD_DATE] = today
            prefs[PreferencesKeys.LAST_AD_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun recordReviewRequested() {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_REVIEW_DATE] = System.currentTimeMillis()
        }
    }
}
