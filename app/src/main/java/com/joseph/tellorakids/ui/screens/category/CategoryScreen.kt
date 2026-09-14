package com.joseph.tellorakids.ui.screens.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.joseph.tellorakids.common.utils.AdsManager
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.ui.components.BannerAdView
import com.joseph.tellorakids.ui.components.ErrorView
import com.joseph.tellorakids.ui.components.StoryCard
import com.joseph.tellorakids.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onBackClick: () -> Unit,
    onStoryClick: (Story) -> Unit,
    onPremiumClick: () -> Unit,
    isExpanded: Boolean = false,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text(text = uiState.categoryName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (!uiState.isPremium) {
                BannerAdView(adUnitId = AdsManager.LIST_BANNER_ID)
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.error != null) {
            ErrorView(
                message = uiState.error,
                onRetry = { viewModel.retry() },
                modifier = Modifier.padding(padding)
            )
        } else if (uiState.stories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(text = "No stories found in this category")
            }
        } else {
            if (isExpanded) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.stories) { story ->
                        StoryCard(
                            story = story,
                            isFavorite = uiState.favoriteIds.contains(story.id),
                            onStoryClick = { 
                                if (story.isPremium && !uiState.isPremium) {
                                    onPremiumClick()
                                } else {
                                    onStoryClick(story)
                                }
                            },
                            onFavoriteClick = { viewModel.toggleFavorite(it) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.stories) { story ->
                        StoryCard(
                            story = story,
                            isFavorite = uiState.favoriteIds.contains(story.id),
                            onStoryClick = { 
                                if (story.isPremium && !uiState.isPremium) {
                                    onPremiumClick()
                                } else {
                                    onStoryClick(story)
                                }
                            },
                            onFavoriteClick = { viewModel.toggleFavorite(it) }
                        )
                    }
                }
            }
        }
    }
}
