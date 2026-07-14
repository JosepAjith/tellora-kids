package com.joseph.tellorakids.domain.usecase

import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class HomeStories(
    val featured: List<Story>,
    val recent: Story?,
    val categories: List<Story>
)

class GetHomeStoriesUseCase @Inject constructor(
    private val repository: StoryRepository
) {
    operator fun invoke(ageGroup: String = "all"): Flow<HomeStories> {
        return combine(
            repository.getFeaturedStories(ageGroup),
            repository.getRecentStoryId(),
            repository.getAllStories(ageGroup)
        ) { featured, recentId, allStories ->
            val recentStory = allStories.find { it.id == recentId }
            HomeStories(
                featured = featured,
                recent = recentStory,
                categories = allStories.distinctBy { it.category }
            )
        }
    }
}
