package com.joseph.tellorakids.domain.repository

import com.joseph.tellorakids.domain.model.Story
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun getAllStories(ageGroup: String = "all"): Flow<List<Story>>
    fun getStoriesByCategory(category: String, ageGroup: String = "all"): Flow<List<Story>>
    fun getStoryById(id: String): Flow<Story?>
    fun getFeaturedStories(ageGroup: String = "all"): Flow<List<Story>>
    fun searchStories(query: String, ageGroup: String = "all"): Flow<List<Story>>
    
    // Favorites
    fun getFavoriteStoryIds(): Flow<Set<String>>
    suspend fun toggleFavorite(storyId: String)
    
    // Continue Reading
    fun getRecentStoryId(): Flow<String?>
    suspend fun saveRecentStory(storyId: String)
    fun getStoryProgress(storyId: String): Flow<Int>
    suspend fun saveStoryProgress(storyId: String, page: Int)

    // Age Filter
    fun getSelectedAgeGroup(): Flow<String>
    suspend fun setSelectedAgeGroup(ageGroup: String)

    // Premium
    fun isPremiumUser(): Flow<Boolean>
    suspend fun setPremiumStatus(isPremium: Boolean)
}
