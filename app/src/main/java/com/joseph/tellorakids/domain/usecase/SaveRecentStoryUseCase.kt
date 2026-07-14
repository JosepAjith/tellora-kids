package com.joseph.tellorakids.domain.usecase

import com.joseph.tellorakids.domain.repository.StoryRepository
import javax.inject.Inject

class SaveRecentStoryUseCase @Inject constructor(
    private val repository: StoryRepository
) {
    suspend operator fun invoke(storyId: String) {
        repository.saveRecentStory(storyId)
    }
}
