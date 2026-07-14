package com.joseph.tellorakids.data.repository

import com.joseph.tellorakids.data.datasource.AssetDataSource
import com.joseph.tellorakids.data.datasource.LocalPreferencesDataSource
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepositoryImpl @Inject constructor(
    private val assetDataSource: AssetDataSource,
    private val preferencesDataSource: LocalPreferencesDataSource
) : StoryRepository {

    override fun getAllStories(ageGroup: String): Flow<List<Story>> = flow {
        val allStories = assetDataSource.getAllStories()
        emit(filterStories(allStories, ageGroup))
    }

    override fun getStoriesByCategory(category: String, ageGroup: String): Flow<List<Story>> = flow {
        val allStories = assetDataSource.getAllStories()
        val categoryStories = allStories.filter { it.category.equals(category, ignoreCase = true) }
        emit(filterStories(categoryStories, ageGroup))
    }

    override fun getStoryById(id: String): Flow<Story?> = flow {
        val allStories = assetDataSource.getAllStories()
        emit(allStories.find { it.id == id })
    }

    override fun getFeaturedStories(ageGroup: String): Flow<List<Story>> = flow {
        val allStories = assetDataSource.getAllStories()
        val featured = allStories.filter { it.isFeatured }
        emit(filterStories(featured, ageGroup))
    }

    override fun searchStories(query: String, ageGroup: String): Flow<List<Story>> = flow {
        val allStories = assetDataSource.getAllStories()
        val searched = allStories.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.category.contains(query, ignoreCase = true) 
        }
        emit(filterStories(searched, ageGroup))
    }

    private fun filterStories(stories: List<Story>, ageGroup: String): List<Story> {
        return if (ageGroup == "all") {
            stories
        } else {
            stories.filter { it.ageGroup == ageGroup }
        }
    }

    override fun getFavoriteStoryIds(): Flow<Set<String>> = preferencesDataSource.favoriteStoryIds

    override suspend fun toggleFavorite(storyId: String) {
        preferencesDataSource.toggleFavorite(storyId)
    }

    override fun getRecentStoryId(): Flow<String?> = preferencesDataSource.recentStoryId

    override suspend fun saveRecentStory(storyId: String) {
        preferencesDataSource.saveRecentStory(storyId)
    }

    override fun getSelectedAgeGroup(): Flow<String> = preferencesDataSource.selectedAgeGroup

    override suspend fun setSelectedAgeGroup(ageGroup: String) {
        preferencesDataSource.setSelectedAgeGroup(ageGroup)
    }

    override fun isPremiumUser(): Flow<Boolean> = preferencesDataSource.isPremium

    override suspend fun setPremiumStatus(isPremium: Boolean) {
        preferencesDataSource.setPremiumStatus(isPremium)
    }
}
