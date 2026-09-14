package com.joseph.tellorakids.data.remote.datasource

import com.joseph.tellorakids.data.remote.dto.SupabaseStoryDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseStoryRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val postgrest = supabaseClient.postgrest

    suspend fun getAllStories(ageGroup: String): List<SupabaseStoryDto> {
        return postgrest["stories"].select(Columns.raw("*, category(*), pages(*)")) {
            filter {
                eq("status", "published")
                if (ageGroup != "all") {
                    eq("age_group", ageGroup)
                }
            }
        }.decodeList<SupabaseStoryDto>()
    }

    suspend fun getFeaturedStories(ageGroup: String): List<SupabaseStoryDto> {
        return postgrest["stories"].select(Columns.raw("*, category(*), pages(*)")) {
            filter {
                eq("status", "published")
                eq("is_featured", true)
                if (ageGroup != "all") {
                    eq("age_group", ageGroup)
                }
            }
        }.decodeList<SupabaseStoryDto>()
    }

    suspend fun getStoriesByCategory(categoryName: String, ageGroup: String): List<SupabaseStoryDto> {
        // We select stories where the joined category name matches
        return postgrest["stories"].select(Columns.raw("*, category(*), pages(*)")) {
            filter {
                eq("status", "published")
                eq("category.name", categoryName)
                if (ageGroup != "all") {
                    eq("age_group", ageGroup)
                }
            }
        }.decodeList<SupabaseStoryDto>()
    }

    suspend fun getStoryById(id: String): SupabaseStoryDto? {
        return postgrest["stories"].select(Columns.raw("*, category(*), pages(*)")) {
            filter {
                eq("id", id)
                eq("status", "published")
            }
        }.decodeSingleOrNull<SupabaseStoryDto>()
    }
}
