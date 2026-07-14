package com.joseph.tellorakids.domain.usecase

import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetFavoriteStoriesUseCase @Inject constructor(
    private val repository: StoryRepository
) {
    operator fun invoke(): Flow<List<Story>> {
        return combine(
            repository.getAllStories(),
            repository.getFavoriteStoryIds()
        ) { allStories, favoriteIds ->
            allStories.filter { favoriteIds.contains(it.id) }
        }
    }
}
