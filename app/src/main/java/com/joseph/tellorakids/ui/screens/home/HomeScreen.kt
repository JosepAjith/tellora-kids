package com.joseph.tellorakids.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.joseph.tellorakids.common.utils.AdsManager
import com.joseph.tellorakids.domain.model.AgeGroup
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.ui.components.BannerAdView
import com.joseph.tellorakids.ui.components.SectionHeader
import com.joseph.tellorakids.ui.components.StoryCard
import com.joseph.tellorakids.ui.theme.*
import com.joseph.tellorakids.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStoryClick: (Story) -> Unit,
    onSearchClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPremiumClick: () -> Unit,
    isExpanded: Boolean = false,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            HomeTopBar(
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
                onPremiumClick = onPremiumClick,
                isPremium = uiState.isPremium,
                isExpanded = isExpanded
            )
        },
        bottomBar = {
            if (!uiState.isPremium) {
                BannerAdView(adUnitId = AdsManager.HOME_BANNER_ID)
            }
        }
    ) { padding ->
        val commonModifier = Modifier
            .padding(padding)
            .fillMaxSize()

        if (isExpanded) {
            HomeTabletLayout(
                uiState = uiState,
                onStoryClick = onStoryClick,
                onCategoryClick = onCategoryClick,
                onFavoritesClick = onFavoritesClick,
                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                onAgeGroupSelected = { viewModel.setAgeGroup(it) },
                modifier = commonModifier.padding(horizontal = 24.dp)
            )
        } else {
            HomePhoneLayout(
                uiState = uiState,
                onStoryClick = onStoryClick,
                onCategoryClick = onCategoryClick,
                onFavoritesClick = onFavoritesClick,
                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                onAgeGroupSelected = { viewModel.setAgeGroup(it) },
                modifier = commonModifier
            )
        }
    }
}

@Composable
fun HomePhoneLayout(
    uiState: com.joseph.tellorakids.viewmodel.HomeUiState,
    onStoryClick: (Story) -> Unit,
    onCategoryClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onAgeGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            AgeFilterRow(
                selectedAge = uiState.selectedAgeGroup,
                onAgeSelected = onAgeGroupSelected
            )
        }

        if (uiState.featuredStories.isNotEmpty()) {
            item {
                SectionHeader(title = "✨ Featured Magic")
                FeaturedPager(
                    stories = uiState.featuredStories,
                    onStoryClick = onStoryClick,
                    cardWidth = 300.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        uiState.recentStory?.let { story ->
            item {
                SectionHeader(title = "📖 Continue Your Journey")
                StoryCard(
                    story = story,
                    isFavorite = uiState.favoriteIds.contains(story.id),
                    onStoryClick = { onStoryClick(story) },
                    onFavoriteClick = onFavoriteToggle,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            SectionHeader(title = "🌈 Explore Worlds")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.categories) { story ->
                    CategoryItem(
                        category = story.category,
                        onClick = { onCategoryClick(story.category) },
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SectionHeader(title = "🆕 New Tales", onViewAllClick = onFavoritesClick)
        }
        
        items(uiState.featuredStories.take(3)) { story ->
            StoryCard(
                story = story,
                isFavorite = uiState.favoriteIds.contains(story.id),
                onStoryClick = { onStoryClick(story) },
                onFavoriteClick = onFavoriteToggle,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun HomeTabletLayout(
    uiState: com.joseph.tellorakids.viewmodel.HomeUiState,
    onStoryClick: (Story) -> Unit,
    onCategoryClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onAgeGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(modifier = Modifier.weight(1.3f)) {
            AgeFilterRow(
                selectedAge = uiState.selectedAgeGroup,
                onAgeSelected = onAgeGroupSelected
            )

            SectionHeader(title = "✨ Featured Magic")
            FeaturedPager(
                stories = uiState.featuredStories,
                onStoryClick = onStoryClick,
                cardWidth = 480.dp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            SectionHeader(title = "🌈 Explore Worlds")
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.categories) { story ->
                    CategoryItem(
                        category = story.category,
                        onClick = { onCategoryClick(story.category) }
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            uiState.recentStory?.let { story ->
                SectionHeader(title = "📖 Continue Journey")
                StoryCard(
                    story = story,
                    isFavorite = uiState.favoriteIds.contains(story.id),
                    onStoryClick = { onStoryClick(story) },
                    onFavoriteClick = onFavoriteToggle,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            SectionHeader(title = "🆕 New Tales", onViewAllClick = onFavoritesClick)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.featuredStories) { story ->
                    StoryCard(
                        story = story,
                        isFavorite = uiState.favoriteIds.contains(story.id),
                        onStoryClick = { onStoryClick(story) },
                        onFavoriteClick = onFavoriteToggle,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AgeFilterRow(
    selectedAge: String,
    onAgeSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(AgeGroup.values()) { group ->
            FilterChip(
                selected = selectedAge == group.filterValue,
                onClick = { onAgeSelected(group.filterValue) },
                label = { Text(group.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, icon) = when(category) {
        "Animals" -> AnimalVibrant to "🦁"
        "Bedtime" -> BedtimeVibrant to "🌙"
        "Moral Stories" -> MoralVibrant to "📜"
        "Friendship" -> FriendshipVibrant to "🤝"
        "Adventure" -> AdventureVibrant to "🚀"
        "Funny" -> FunnyVibrant to "🤡"
        else -> MaterialTheme.colorScheme.secondaryContainer to "📚"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = bgColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(2.dp, bgColor.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 12.dp)
        ) {
            Text(text = icon, fontSize = 44.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun HomeTopBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPremiumClick: () -> Unit,
    isPremium: Boolean,
    isExpanded: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tellora Kids",
                style = if (isExpanded) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
            Text(
                text = "A storytelling world for kids",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isPremium) {
                Surface(
                    onClick = onPremiumClick,
                    color = Color(0xFFFFD700),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = Color(0xFF5D4037),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Surface(
                onClick = onSearchClick,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Surface(
                onClick = onSettingsClick,
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedPager(
    stories: List<Story>,
    onStoryClick: (Story) -> Unit,
    cardWidth: androidx.compose.ui.unit.Dp
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(stories) { story ->
            Card(
                modifier = Modifier
                    .width(cardWidth)
                    .height(if (cardWidth > 400.dp) 280.dp else 200.dp)
                    .clickable { onStoryClick(story) },
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = story.coverImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                    startY = 150f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "FEATURED",
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (story.isPremium) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Star, contentDescription = "Premium", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = story.title,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
