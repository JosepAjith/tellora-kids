package com.joseph.tellorakids.domain.model

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Story(
    val id: String = "",
    val version: Int = 1,
    val title: String = "",
    val category: String = "",
    val coverImage: String = "",
    val ageGroup: String = "",
    val readingTime: String = "",
    val language: String = "en",
    val pages: List<StoryPage> = emptyList(),
    val moral: String = "",
    
    @get:PropertyName("featured") @set:PropertyName("featured")
    var isFeatured: Boolean = false,
    
    @get:PropertyName("premium") @set:PropertyName("premium")
    var isPremium: Boolean = false
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class StoryPage(
    val page: Int = 0,
    val image: String = "",
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
