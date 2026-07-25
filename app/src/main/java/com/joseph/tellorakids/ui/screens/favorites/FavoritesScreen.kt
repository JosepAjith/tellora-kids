package com.joseph.tellorakids.ui.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.ui.components.ErrorView
import com.joseph.tellorakids.ui.components.StoryCard
import com.joseph.tellorakids.ui.screens.home.EmptyState
import com.joseph.tellorakids.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onStoryClick: (Story) -> Unit,
    isExpanded: Boolean = false,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(padding)

        if (uiState.isLoading) {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.error != null) {
            ErrorView(
                message = uiState.error,
                onRetry = { viewModel.retry() },
                modifier = modifier
            )
        } else {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 12.dp)
                    ) {
                        Text(
                            text = "My Favorites",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Stories you love",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (uiState.favoriteStories.isEmpty()) {
                    item {
                        EmptyState(
                            message = "Your favorites will appear here",
                            icon = "❤️",
                            onRetry = null
                        )
                    }
                } else {
                    items(uiState.favoriteStories) { story ->
                        StoryCard(
                            story = story,
                            isFavorite = true,
                            onStoryClick = { onStoryClick(story) },
                            onFavoriteClick = { viewModel.toggleFavorite(it) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}
