package com.joseph.tellorakids.domain.usecase

import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchStoriesUseCase @Inject constructor(
    private val repository: StoryRepository
) {
    operator fun invoke(query: String, ageGroup: String = "all"): Flow<List<Story>> {
        return repository.searchStories(query, ageGroup)
    }
}
