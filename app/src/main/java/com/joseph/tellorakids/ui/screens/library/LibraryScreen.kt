package com.joseph.tellorakids.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.joseph.tellorakids.ui.components.SectionHeader
import com.joseph.tellorakids.ui.components.StoryCard
import com.joseph.tellorakids.ui.screens.home.ContinueReadingCard
import com.joseph.tellorakids.ui.screens.home.EmptyState
import com.joseph.tellorakids.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    onStoryClick: (Story) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(padding)

        if (uiState.isLoading) {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            ErrorView(
                message = uiState.error!!,
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
                            text = "My Library",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Your magical collection",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (uiState.continueReading.isEmpty() && uiState.favorites.isEmpty() && uiState.completed.isEmpty()) {
                    item {
                        EmptyState(
                            message = "Your library is empty. Start your first adventure!",
                            icon = "📚",
                            onRetry = null
                        )
                    }
                } else {
                    if (uiState.continueReading.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Continue Reading", icon = "📖")
                        }
                        items(uiState.continueReading) { libStory ->
                            ContinueReadingCard(
                                story = libStory.story,
                                currentPage = libStory.currentPage,
                                onContinueClick = { onStoryClick(libStory.story) },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    if (uiState.favorites.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Your Favorites", icon = "❤️")
                        }
                        items(uiState.favorites) { libStory ->
                            StoryCard(
                                story = libStory.story,
                                isFavorite = true,
                                onStoryClick = { onStoryClick(libStory.story) },
                                onFavoriteClick = { viewModel.toggleFavorite(it) },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    if (uiState.completed.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Completed Stories", icon = "🌟")
                        }
                        items(uiState.completed) { libStory ->
                            StoryCard(
                                story = libStory.story,
                                isFavorite = libStory.isFavorite,
                                onStoryClick = { onStoryClick(libStory.story) },
                                onFavoriteClick = { viewModel.toggleFavorite(it) },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
