package com.joseph.tellorakids.model

data class StoryPage(
    val text: String,
    val imageUrl: String,
    val audioUrl: String? = null
)

data class Story(
    val id: String,
    val title: String,
    val pages: List<StoryPage>
)
