package com.joseph.tellorakids.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.joseph.tellorakids.ui.screens.library.LibraryScreen
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
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                NavigationRailItem(
                    selected = currentRoute == Screen.Home.route,
                    onClick = { 
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(if (currentRoute == Screen.Home.route) Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Home", modifier = Modifier.size(24.dp)) },
                    label = { Text("Home") }
                )
                NavigationRailItem(
                    selected = currentRoute == Screen.Search.route,
                    onClick = { 
                        navController.navigate(Screen.Search.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(if (currentRoute == Screen.Search.route) Icons.Default.Search else Icons.Outlined.Search, contentDescription = "Search", modifier = Modifier.size(24.dp)) },
                    label = { Text("Browse") }
                )
                NavigationRailItem(
                    selected = currentRoute == Screen.Favorites.route,
                    onClick = { 
                        navController.navigate(Screen.Favorites.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(if (currentRoute == Screen.Favorites.route) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Favorites", modifier = Modifier.size(24.dp)) },
                    label = { Text("Favorites") }
                )
                NavigationRailItem(
                    selected = currentRoute == Screen.Library.route,
                    onClick = { 
                        navController.navigate(Screen.Library.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(if (currentRoute == Screen.Library.route) Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "Library", modifier = Modifier.size(24.dp)) },
                    label = { Text("Library") }
                )
            }
            NavHostContainer(navController = navController, isExpanded = isExpanded, modifier = Modifier.weight(1f))
        }
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                val showBottomBar = currentRoute in listOf(
                    Screen.Home.route,
                    Screen.Search.route,
                    Screen.Favorites.route,
                    Screen.Library.route
                )
                if (showBottomBar) {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == Screen.Home.route,
                                onClick = { 
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { 
                                    Icon(
                                        imageVector = if (currentRoute == Screen.Home.route) Icons.Default.Home else Icons.Outlined.Home, 
                                        contentDescription = "Home"
                                    ) 
                                },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == Screen.Search.route,
                                onClick = { 
                                    navController.navigate(Screen.Search.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { 
                                    Icon(
                                        imageVector = if (currentRoute == Screen.Search.route) Icons.Default.Search else Icons.Outlined.Search, 
                                        contentDescription = "Browse"
                                    ) 
                                },
                                label = { Text("Browse") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == Screen.Favorites.route,
                                onClick = { 
                                    navController.navigate(Screen.Favorites.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { 
                                    Icon(
                                        imageVector = if (currentRoute == Screen.Favorites.route) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, 
                                        contentDescription = "Favorites"
                                    ) 
                                },
                                label = { Text("Favorites") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == Screen.Library.route,
                                onClick = { 
                                    navController.navigate(Screen.Library.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { 
                                    Icon(
                                        imageVector = if (currentRoute == Screen.Library.route) Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.MenuBook, 
                                        contentDescription = "Library"
                                    ) 
                                },
                                label = { Text("Library") }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            // Use only top padding from scaffold to allow content to go behind bottom bar
            NavHostContainer(
                navController = navController, 
                isExpanded = isExpanded,
                modifier = Modifier.padding(top = padding.calculateTopPadding())
            )
        }
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

        composable(Screen.Library.route) {
            val viewModel: com.joseph.tellorakids.viewmodel.LibraryViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LibraryScreen(
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
