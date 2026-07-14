package com.joseph.tellorakids.domain.usecase

import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class StoryDetails(
    val story: Story,
    val isFavorite: Boolean
)

class GetStoryDetailsUseCase @Inject constructor(
    private val repository: StoryRepository
) {
    operator fun invoke(storyId: String): Flow<StoryDetails?> {
        return combine(
            repository.getStoryById(storyId),
            repository.getFavoriteStoryIds()
        ) { story, favorites ->
            story?.let {
                StoryDetails(
                    story = it,
                    isFavorite = favorites.contains(it.id)
                )
            }
        }
    }
}
