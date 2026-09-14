package com.joseph.tellorakids.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.joseph.tellorakids.domain.model.StoryPage

@Entity(
    tableName = "cached_story_pages",
    foreignKeys = [
        ForeignKey(
            entity = StoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["storyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["storyId"])]
)
data class StoryPageEntity(
    @PrimaryKey val id: String,
    val storyId: String,
    val pageNumber: Int,
    val text: String,
    val image: String,
    val imageUrl: String
)

fun StoryPageEntity.toDomain(): StoryPage {
    return StoryPage(
        id = id,
        page = pageNumber,
        text = text,
        image = image,
        imageUrl = imageUrl
    )
}

fun StoryPage.toEntity(storyId: String): StoryPageEntity {
    return StoryPageEntity(
        id = id,
        storyId = storyId,
        pageNumber = page,
        text = text,
        image = image,
        imageUrl = imageUrl
    )
}
