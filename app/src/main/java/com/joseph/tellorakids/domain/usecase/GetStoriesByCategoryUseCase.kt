package com.joseph.tellorakids.domain.usecase

import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoriesByCategoryUseCase @Inject constructor(
    private val repository: StoryRepository
) {
    operator fun invoke(category: String, ageGroup: String = "all"): Flow<List<Story>> {
        return repository.getStoriesByCategory(category, ageGroup)
    }
}
