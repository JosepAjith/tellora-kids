package com.joseph.tellorakids.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.joseph.tellorakids.common.utils.AdsManager
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.ui.components.BannerAdView
import com.joseph.tellorakids.ui.components.StoryCard
import com.joseph.tellorakids.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onStoryClick: (Story) -> Unit,
    isExpanded: Boolean = false,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        placeholder = { Text("Search stories...") },
                        leadingIcon = null,
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )
                },
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
        if (uiState.results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.query.isEmpty()) "Search for stories by title or category" else "No stories found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                    items(uiState.results) { story ->
                        StoryCard(
                            story = story,
                            isFavorite = uiState.favoriteIds.contains(story.id),
                            onStoryClick = { onStoryClick(story) },
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
                    items(uiState.results) { story ->
                        StoryCard(
                            story = story,
                            isFavorite = uiState.favoriteIds.contains(story.id),
                            onStoryClick = { onStoryClick(story) },
                            onFavoriteClick = { viewModel.toggleFavorite(it) }
                        )
                    }
                }
            }
        }
    }
}
