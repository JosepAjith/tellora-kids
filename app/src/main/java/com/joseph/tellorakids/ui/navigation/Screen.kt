package com.joseph.tellorakids.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CategoryList : Screen("category_list/{categoryName}") {
        fun createRoute(categoryName: String) = "category_list/$categoryName"
    }
    object StoryDetails : Screen("story_details/{storyId}") {
        fun createRoute(storyId: String) = "story_details/$storyId"
    }
    object Favorites : Screen("favorites")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Library : Screen("library")
    object Premium : Screen("premium")
}
