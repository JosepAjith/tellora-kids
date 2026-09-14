package com.joseph.tellorakids.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.joseph.tellorakids.domain.model.Story

@Entity(tableName = "cached_stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val storyId: String,
    val title: String,
    val category: String,
    val coverImage: String,
    val coverImageUrl: String,
    val ageGroup: String,
    val readingTime: String,
    val language: String,
    val description: String,
    val music: String,
    val status: String,
    val moral: String,
    val createdAt: String,
    val updatedAt: String,
    val isFeatured: Boolean,
    val isPremium: Boolean
)

fun StoryEntity.toDomain(pages: List<com.joseph.tellorakids.domain.model.StoryPage>): Story {
    return Story(
        id = id,
        storyId = storyId,
        title = title,
        category = category,
        coverImage = coverImage,
        coverImageUrl = coverImageUrl,
        ageGroup = ageGroup,
        readingTime = readingTime,
        language = language,
        description = description,
        music = music,
        status = status,
        pages = pages,
        moral = moral,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFeatured = isFeatured,
        isPremium = isPremium
    )
}

fun Story.toEntity(): StoryEntity {
    return StoryEntity(
        id = id,
        storyId = storyId,
        title = title,
        category = category,
        coverImage = coverImage,
        coverImageUrl = coverImageUrl,
        ageGroup = ageGroup,
        readingTime = readingTime,
        language = language,
        description = description,
        music = music,
        status = status,
        moral = moral,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFeatured = isFeatured,
        isPremium = isPremium
    )
}
