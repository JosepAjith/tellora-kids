package com.joseph.tellorakids.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.ui.screens.category.CategoryScreen
import com.joseph.tellorakids.ui.screens.favorites.FavoritesScreen
import com.joseph.tellorakids.ui.screens.home.HomeScreen
import com.joseph.tellorakids.ui.screens.premium.PremiumScreen
import com.joseph.tellorakids.ui.screens.search.SearchScreen
import com.joseph.tellorakids.ui.screens.settings.SettingsScreen
import com.joseph.tellorakids.ui.screens.story.StoryScreen

@Composable
fun TelloraKidsNavigation(
    navController: NavHostController,
    isExpanded: Boolean
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (isExpanded) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail {
                NavigationRailItem(
                    selected = currentRoute == Screen.Home.route,
                    onClick = { navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    } },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationRailItem(
                    selected = currentRoute == Screen.Search.route,
                    onClick = { navController.navigate(Screen.Search.route) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationRailItem(
                    selected = currentRoute == Screen.Favorites.route,
                    onClick = { navController.navigate(Screen.Favorites.route) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") }
                )
                NavigationRailItem(
                    selected = currentRoute == Screen.Settings.route,
                    onClick = { navController.navigate(Screen.Settings.route) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
            NavHostContainer(navController = navController, isExpanded = isExpanded, modifier = Modifier.weight(1f))
        }
    } else {
        NavHostContainer(navController = navController, isExpanded = isExpanded)
    }
}

@Composable
fun NavHostContainer(
    navController: NavHostController,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val viewModel: com.joseph.tellorakids.viewmodel.HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            HomeScreen(
                onStoryClick = { story ->
                    if (story.isPremium && !uiState.isPremium) {
                        navController.navigate(Screen.Premium.route)
                    } else {
                        navController.navigate(Screen.StoryDetails.createRoute(story.id))
                    }
                },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onCategoryClick = { categoryName ->
                    navController.navigate(Screen.CategoryList.createRoute(categoryName))
                },
                onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onPremiumClick = { navController.navigate(Screen.Premium.route) },
                isExpanded = isExpanded,
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.CategoryList.route,
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) {
            val viewModel: com.joseph.tellorakids.viewmodel.CategoryViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            CategoryScreen(
                isExpanded = isExpanded,
                onBackClick = { navController.popBackStack() },
                onStoryClick = { story ->
                    if (story.isPremium && !uiState.isPremium) {
                        navController.navigate(Screen.Premium.route)
                    } else {
                        navController.navigate(Screen.StoryDetails.createRoute(story.id))
                    }
                },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.StoryDetails.route,
            arguments = listOf(navArgument("storyId") { type = NavType.StringType })
        ) {
            StoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            val viewModel: com.joseph.tellorakids.viewmodel.SearchViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            SearchScreen(
                isExpanded = isExpanded,
                onBackClick = { navController.popBackStack() },
                onStoryClick = { story ->
                    if (story.isPremium && !uiState.isPremium) {
                        navController.navigate(Screen.Premium.route)
                    } else {
                        navController.navigate(Screen.StoryDetails.createRoute(story.id))
                    }
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Favorites.route) {
            val viewModel: com.joseph.tellorakids.viewmodel.FavoritesViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            FavoritesScreen(
                isExpanded = isExpanded,
                onBackClick = { navController.popBackStack() },
                onStoryClick = { story ->
                    if (story.isPremium && !uiState.isPremium) {
                        navController.navigate(Screen.Premium.route)
                    } else {
                        navController.navigate(Screen.StoryDetails.createRoute(story.id))
                    }
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onPremiumClick = { navController.navigate(Screen.Premium.route) }
            )
        }

        composable(Screen.Premium.route) {
            PremiumScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
