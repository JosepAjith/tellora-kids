package com.joseph.tellorakids.data.datasource

import android.content.Context
import com.joseph.tellorakids.domain.model.Story
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun getAllStories(): List<Story> = withContext(Dispatchers.IO) {
        val stories = mutableListOf<Story>()
        val categories = context.assets.list("stories") ?: emptyArray()
        
        for (category in categories) {
            val storyFiles = context.assets.list("stories/$category") ?: emptyArray()
            for (fileName in storyFiles) {
                if (fileName.endsWith(".json")) {
                    try {
                        val story = readStoryFile("stories/$category/$fileName")
                        stories.add(story)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        stories
    }

    private fun readStoryFile(path: String): Story {
        context.assets.open(path).use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                return json.decodeFromString(reader.readText())
            }
        }
    }
}
