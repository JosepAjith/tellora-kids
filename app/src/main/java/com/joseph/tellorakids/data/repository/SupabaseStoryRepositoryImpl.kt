package com.joseph.tellorakids.data.repository

import com.joseph.tellorakids.data.datasource.LocalPreferencesDataSource
import com.joseph.tellorakids.data.local.dao.StoryDao
import com.joseph.tellorakids.data.local.entity.toDomain
import com.joseph.tellorakids.data.local.entity.toEntity
import com.joseph.tellorakids.data.remote.datasource.SupabaseStoryRemoteDataSource
import com.joseph.tellorakids.data.remote.dto.toDomain
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.repository.StoryRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseStoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: SupabaseStoryRemoteDataSource,
    private val storyDao: StoryDao,
    private val preferencesDataSource: LocalPreferencesDataSource
) : StoryRepository {

    override fun getAllStories(ageGroup: String): Flow<List<Story>> = flow {
        // 1. Emit cached data first
        val cachedStories = storyDao.getAllStories().first()
        emit(cachedStories.map { it.toDomain(emptyList()) })

        // 2. Fetch from remote
        try {
            val remoteDtos = remoteDataSource.getAllStories(ageGroup)
            val remoteStories = remoteDtos.map { it.toDomain() }
            
            // 3. Update cache (for simplicity in this phase, we refresh what we got)
            // In a real app, we might want more complex sync logic
            storyDao.insertStories(remoteStories.map { it.toEntity() })
            
            emit(remoteStories)
        } catch (e: Exception) {
            // If remote fails, we already emitted cache. Log error.
            android.util.Log.e("SupabaseRepo", "Failed to fetch stories", e)
        }
    }

    override fun getFeaturedStories(ageGroup: String): Flow<List<Story>> = flow {
        // Initially emit from cache filtered by featured
        val cached = storyDao.getAllStories().first()
            .filter { it.isFeatured }
            .map { it.toDomain(emptyList()) }
        emit(cached)

        try {
            val remote = remoteDataSource.getFeaturedStories(ageGroup).map { it.toDomain() }
            storyDao.insertStories(remote.map { it.toEntity() })
            emit(remote)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "Failed to fetch featured", e)
        }
    }

    override fun getStoriesByCategory(category: String, ageGroup: String): Flow<List<Story>> = flow {
        val cached = storyDao.getAllStories().first()
            .filter { it.category == category }
            .map { it.toDomain(emptyList()) }
        emit(cached)

        try {
            val remote = remoteDataSource.getStoriesByCategory(category, ageGroup).map { it.toDomain() }
            storyDao.insertStories(remote.map { it.toEntity() })
            emit(remote)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "Failed to fetch category $category", e)
        }
    }

    override fun getStoryById(id: String): Flow<Story?> = flow {
        // Combine cache and pages
        val cachedStory = storyDao.getStoryById(id).first()
        val cachedPages = storyDao.getPagesForStory(id).first()
        
        if (cachedStory != null) {
            emit(cachedStory.toDomain(cachedPages.map { it.toDomain() }))
        }

        try {
            val remoteDto = remoteDataSource.getStoryById(id)
            if (remoteDto != null) {
                val remoteStory = remoteDto.toDomain()
                // Update cache
                storyDao.insertStories(listOf(remoteStory.toEntity()))
                storyDao.insertPages(remoteStory.pages.map { it.toEntity(remoteStory.id) })
                emit(remoteStory)
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "Failed to fetch story $id", e)
        }
    }

    override fun searchStories(query: String, ageGroup: String): Flow<List<Story>> = flow {
        // Simple client-side search over cache first
        val cached = storyDao.getAllStories().first()
            .filter { it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
            .map { it.toDomain(emptyList()) }
        emit(cached)

        // For now, continuing the client-side filter over the complete list from remote
        try {
            val allRemote = remoteDataSource.getAllStories(ageGroup)
            val filtered = allRemote
                .filter { it.title.orEmpty().contains(query, ignoreCase = true) || it.category.orEmpty().contains(query, ignoreCase = true) }
                .map { it.toDomain() }
            emit(filtered)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "Search failed", e)
        }
    }

    // Favorites (Keep local as requested)
    override fun getFavoriteStoryIds(): Flow<Set<String>> = preferencesDataSource.favoriteStoryIds
    override suspend fun toggleFavorite(storyId: String) = preferencesDataSource.toggleFavorite(storyId)

    // Continue Reading (Keep local as requested)
    override fun getRecentStoryId(): Flow<String?> = preferencesDataSource.recentStoryId
    override suspend fun saveRecentStory(storyId: String) = preferencesDataSource.saveRecentStory(storyId)
    override fun getStoryProgress(storyId: String): Flow<Int> = preferencesDataSource.getStoryProgress(storyId)
    override suspend fun saveStoryProgress(storyId: String, page: Int) = preferencesDataSource.saveStoryProgress(storyId, page)

    // Age Filter (Keep local as requested)
    override fun getSelectedAgeGroup(): Flow<String> = preferencesDataSource.selectedAgeGroup
    override suspend fun setSelectedAgeGroup(ageGroup: String) = preferencesDataSource.setSelectedAgeGroup(ageGroup)

    // Premium (Keep local as requested)
    override fun isPremiumUser(): Flow<Boolean> = preferencesDataSource.isPremium
    override suspend fun setPremiumStatus(isPremium: Boolean) = preferencesDataSource.setPremiumStatus(isPremium)
}
