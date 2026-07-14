package com.joseph.tellorakids.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val id: String,
    val title: String,
    val category: String,
    val coverImage: String,
    val ageGroup: String, // Updated to ageGroup for clarity (e.g., "2-3", "4-5", "6-8")
    val readingTime: String,
    val pages: List<StoryPage>,
    val moral: String,
    val isFeatured: Boolean = false,
    val isPremium: Boolean = false // New: Added for premium upgrade feature
)

@Serializable
data class StoryPage(
    val image: String,
    val text: String
)

enum class StoryCategory(val displayName: String) {
    ANIMALS("Animals"),
    BEDTIME("Bedtime"),
    MORAL("Moral Stories"),
    FRIENDSHIP("Friendship"),
    ADVENTURE("Adventure"),
    FUNNY("Funny")
}

enum class AgeGroup(val displayName: String, val filterValue: String) {
    ALL("All", "all"),
    TWO_THREE("2–3 Years", "2-3"),
    FOUR_FIVE("4–5 Years", "4-5"),
    SIX_EIGHT("6–8 Years", "6-8")
}
