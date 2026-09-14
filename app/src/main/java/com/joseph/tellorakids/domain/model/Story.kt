package com.joseph.tellorakids.domain.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Story(
    val id: String = "",
    val storyId: String = "",
    val version: Int = 1,
    val title: String = "",
    val category: String = "",
    val coverImage: String = "",
    val coverImageUrl: String = "",
    val ageGroup: String = "",
    val readingTime: String = "",
    val language: String = "en",
    val description: String = "",
    val music: String = "",
    val status: String = "",
    val pages: List<StoryPage> = emptyList(),
    val moral: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    
    val isFeatured: Boolean = false,
    val isPremium: Boolean = false
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class StoryPage(
    val id: String = "",
    val page: Int = 0,
    val image: String = "",
    val imageUrl: String = "",
    val text: String = ""
)

enum class StoryCategory(val displayName: String) {
    ANIMALS("Animals"),
    BEDTIME("Bedtime"),
    MORAL("Moral Stories"),
    FRIENDSHIP("Friendship"),
    ADVENTURE("Adventure"),
    FUNNY("Funny"),
    NATURE("Nature")
}

enum class AgeGroup(val displayName: String, val filterValue: String) {
    ALL("All", "all"),
    TWO_FOUR("2–4 Years", "2-4"),
    FOUR_SIX("4–6 Years", "4-6"),
    SIX_EIGHT("6–8 Years", "6-8")
}
