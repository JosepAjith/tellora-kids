package com.joseph.tellorakids.data.local.dao

import androidx.room.*
import com.joseph.tellorakids.data.local.entity.StoryEntity
import com.joseph.tellorakids.data.local.entity.StoryPageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM cached_stories WHERE status = 'published'")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM cached_stories WHERE id = :id")
    fun getStoryById(id: String): Flow<StoryEntity?>

    @Query("SELECT * FROM cached_story_pages WHERE storyId = :storyId ORDER BY pageNumber ASC")
    fun getPagesForStory(storyId: String): Flow<List<StoryPageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<StoryPageEntity>)

    @Query("DELETE FROM cached_stories")
    suspend fun clearAllStories()

    @Transaction
    suspend fun refreshStories(stories: List<StoryEntity>, pages: List<StoryPageEntity>) {
        clearAllStories()
        insertStories(stories)
        insertPages(pages)
    }
}
