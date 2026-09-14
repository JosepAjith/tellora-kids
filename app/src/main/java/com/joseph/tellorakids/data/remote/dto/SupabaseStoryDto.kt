package com.joseph.tellorakids.data.remote.dto

import com.joseph.tellorakids.data.util.StoryAssetResolver
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.model.StoryPage
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class SupabaseStoryDto(
    @SerialName("id") val id: String,
    @SerialName("slug") val slug: String? = null,
    @SerialName("title") val title: String? = "",
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("category") val category: SupabaseCategoryDto? = null,
    @SerialName("age_group") val ageGroup: String? = "all",
    @SerialName("reading_time_minutes") val readingTimeMinutes: Int? = 0,
    @SerialName("description") val description: String? = null,
    @SerialName("moral") val moral: String? = null,
    @SerialName("cover_object_key") val coverObjectKey: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("status") val status: String? = "published",
    @SerialName("is_premium") val isPremium: Boolean = false,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("version") val version: Int = 1,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("pages") val pages: List<SupabaseStoryPageDto>? = emptyList()
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class SupabaseCategoryDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String? = null
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class SupabaseStoryPageDto(
    @SerialName("id") val id: String,
    @SerialName("position") val position: Int = 0,
    @SerialName("text") val text: String? = "",
    @SerialName("image_object_key") val imageObjectKey: String? = null,
    @SerialName("image_url") val imageUrl: String? = null
)

fun SupabaseStoryDto.toDomain(): Story {
    return Story(
        id = id,
        storyId = id,
        version = version,
        title = title ?: "",
        category = category?.name ?: "General",
        coverImage = StoryAssetResolver.resolve(coverUrl ?: coverObjectKey),
        coverImageUrl = StoryAssetResolver.resolve(coverUrl ?: coverObjectKey),
        ageGroup = ageGroup ?: "all",
        readingTime = "${readingTimeMinutes ?: 0} min",
        description = description ?: "",
        status = status ?: "published",
        moral = moral ?: "",
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: "",
        isFeatured = isFeatured,
        isPremium = isPremium,
        pages = (pages ?: emptyList()).map { it.toDomain() }.sortedBy { it.page }
    )
}

fun SupabaseStoryPageDto.toDomain(): StoryPage {
    return StoryPage(
        id = id,
        page = position,
        text = text ?: "",
        image = StoryAssetResolver.resolve(imageUrl ?: imageObjectKey),
        imageUrl = StoryAssetResolver.resolve(imageUrl ?: imageObjectKey)
    )
}
