package com.joseph.tellorakids.ui.screens.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.joseph.tellorakids.common.utils.AdsManager
import com.joseph.tellorakids.domain.model.AgeGroup
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.ui.components.BannerAdView
import com.joseph.tellorakids.ui.components.ErrorView
import com.joseph.tellorakids.ui.components.SectionHeader
import com.joseph.tellorakids.ui.components.StoryCard
import com.joseph.tellorakids.ui.theme.*
import com.joseph.tellorakids.viewmodel.HomeViewModel
import java.util.Calendar

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
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                onSettingsClick = onSettingsClick,
                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                onAgeGroupSelected = { viewModel.setAgeGroup(it) },
                onRetry = { viewModel.retry() },
                modifier = commonModifier
            )
        } else {
            HomePhoneLayout(
                uiState = uiState,
                onStoryClick = onStoryClick,
                onCategoryClick = onCategoryClick,
                onFavoritesClick = onFavoritesClick,
                onSettingsClick = onSettingsClick,
                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                onAgeGroupSelected = { viewModel.setAgeGroup(it) },
                onRetry = { viewModel.retry() },
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
    onSettingsClick: () -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onAgeGroupSelected: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            HomeTopBar(
                onSettingsClick = onSettingsClick,
                isExpanded = false
            )
        }

        item {
            WelcomeCard()
        }

        item {
            AgeFilterRow(
                availableAges = uiState.availableAgeGroups,
                selectedAge = uiState.selectedAgeGroup,
                onAgeSelected = onAgeGroupSelected
            )
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (uiState.error != null) {
            item {
                EmptyState(
                    message = uiState.error ?: "Something went wrong",
                    icon = "🌪️",
                    onRetry = onRetry
                )
            }
        } else if (uiState.featuredStories.isEmpty() && uiState.categories.isEmpty()) {
            item {
                EmptyState(
                    message = "No stories found for this age group. Try another one!",
                    icon = "✨",
                    onRetry = { onAgeGroupSelected("all") }
                )
            }
        } else {
            if (uiState.featuredStories.isNotEmpty()) {
                item {
                    SectionHeader(title = "Featured Magic", icon = "✨")
                    FeaturedPager(
                        stories = uiState.featuredStories,
                        onStoryClick = onStoryClick,
                        cardWidth = 320.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            uiState.recentStory?.let { story ->
                item {
                    SectionHeader(title = "Continue Reading", icon = "📖")
                    ContinueReadingCard(
                        story = story,
                        currentPage = uiState.recentStoryProgress,
                        onContinueClick = { onStoryClick(story) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (uiState.categories.isNotEmpty()) {
                item {
                    SectionHeader(title = "Explore Worlds", icon = "🌈")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.categories) { story ->
                            CategoryItem(
                                category = story.category,
                                onClick = { onCategoryClick(story.category) },
                                modifier = Modifier.width(140.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                SectionHeader(
                    title = "New Stories", 
                    icon = "🆕",
                    onViewAllClick = onFavoritesClick
                )
            }
            
            items(uiState.featuredStories.take(5)) { story ->
                StoryCard(
                    story = story,
                    isFavorite = uiState.favoriteIds.contains(story.id),
                    onStoryClick = { onStoryClick(story) },
                    onFavoriteClick = onFavoriteToggle,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            if (!uiState.isPremium) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BannerAdView(adUnitId = AdsManager.HOME_BANNER_ID)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTabletLayout(
    uiState: com.joseph.tellorakids.viewmodel.HomeUiState,
    onStoryClick: (Story) -> Unit,
    onCategoryClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onAgeGroupSelected: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(modifier = Modifier.weight(1.4f)) {
            HomeTopBar(
                onSettingsClick = onSettingsClick,
                isExpanded = true
            )

            WelcomeCard()
            
            AgeFilterRow(
                availableAges = uiState.availableAgeGroups,
                selectedAge = uiState.selectedAgeGroup,
                onAgeSelected = onAgeGroupSelected
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.featuredStories.isEmpty() && uiState.categories.isEmpty()) {
                EmptyState(
                    message = "No stories found. Try another magic age!",
                    icon = "✨",
                    onRetry = { onAgeGroupSelected("all") }
                )
            } else {
                SectionHeader(title = "Featured Magic", icon = "✨")
                FeaturedPager(
                    stories = uiState.featuredStories,
                    onStoryClick = onStoryClick,
                    cardWidth = 520.dp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                SectionHeader(title = "Explore Worlds", icon = "🌈")
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(bottom = 32.dp, start = 20.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 1000.dp)
                ) {
                    items(uiState.categories) { story ->
                        CategoryItem(
                            category = story.category,
                            onClick = { onCategoryClick(story.category) }
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            uiState.recentStory?.let { story ->
                SectionHeader(title = "Continue Journey", icon = "📖")
                ContinueReadingCard(
                    story = story,
                    currentPage = uiState.recentStoryProgress,
                    onContinueClick = { onStoryClick(story) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            SectionHeader(
                title = "New Stories", 
                icon = "🆕",
                onViewAllClick = onFavoritesClick
            )
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
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AgeFilterRow(
    availableAges: List<String>,
    selectedAge: String,
    onAgeSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Always show "All"
        item {
            AgeChip(
                displayName = "All",
                filterValue = "all",
                isSelected = selectedAge == "all",
                onClick = { onAgeSelected("all") }
            )
        }
        
        items(availableAges) { age ->
            AgeChip(
                displayName = "$age Years",
                filterValue = age,
                isSelected = selectedAge == age,
                onClick = { onAgeSelected(age) }
            )
        }
    }
}

@Composable
fun AgeChip(
    displayName: String,
    filterValue: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = if (isSelected) 4.dp else 0.dp,
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when {
                filterValue == "all" -> "✨"
                filterValue.contains("2") -> "🧸"
                filterValue.contains("4") -> "🌈"
                filterValue.contains("6") -> "🚀"
                else -> "📚"
            }
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WelcomeCard() {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
    val emoji = when (hour) {
        in 0..11 -> "☀️"
        in 12..16 -> "🌈"
        else -> "🌙"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Decoration
            Text(
                text = emoji,
                fontSize = 80.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 0.dp)
                    .alpha(0.15f)
            )

            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "$greeting 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ready for today's adventure?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Read together and discover wonderful stories.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ContinueReadingCard(
    story: Story,
    currentPage: Int,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onContinueClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = story.coverImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = story.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val totalPages = story.pages.size.takeIf { it > 0 } ?: 1
                val progress = (currentPage.toFloat() / totalPages).coerceIn(0f, 1f)
                val isCompleted = currentPage >= totalPages && totalPages > 0

                Text(
                    text = if (isCompleted) "Completed! ✨" else "Page $currentPage of $totalPages • ${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    trackColor = (if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary).copy(alpha = 0.1f)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onContinueClick,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    if (currentPage >= (story.pages.size.takeIf { it > 0 } ?: 1)) Icons.Rounded.Replay else Icons.Rounded.PlayArrow,
                    contentDescription = "Continue",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onFavoritesClick: () -> Unit,
    onSurpriseClick: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            QuickActionCard(
                title = "Surprise Me",
                icon = "🎲",
                color = KidsOrange,
                onClick = onSurpriseClick
            )
        }
        item {
            QuickActionCard(
                title = "Favorites",
                icon = "❤️",
                color = SoftPink,
                onClick = onFavoritesClick
            )
        }
        item {
            QuickActionCard(
                title = "Bedtime",
                icon = "🌙",
                color = SoftBlue,
                onClick = { onCategoryClick("Bedtime") }
            )
        }
        item {
            QuickActionCard(
                title = "Popular",
                icon = "🔥",
                color = SoftGreen,
                onClick = { /* Handle popular */ }
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.2f),
        modifier = Modifier.width(130.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    icon: String,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 60.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Try Again")
            }
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
        "Nature" -> TertiaryVibrant to "🌲"
        else -> MaterialTheme.colorScheme.secondaryContainer to "📚"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        color = bgColor.copy(alpha = 0.12f),
        border = BorderStroke(2.dp, bgColor.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = bgColor.copy(alpha = 0.2f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 36.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun HomeTopBar(
    onSettingsClick: () -> Unit,
    isExpanded: Boolean = false
) {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tellora Kids",
                style = if (isExpanded) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-0.5).sp,
                maxLines = 1
            )
            Text(
                text = "A storytelling world for kids",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Surface(
            onClick = onSettingsClick,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
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
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(stories) { story ->
            Surface(
                onClick = { onStoryClick(story) },
                modifier = Modifier
                    .width(cardWidth)
                    .height(if (cardWidth > 400.dp) 320.dp else 220.dp),
                shape = RoundedCornerShape(32.dp),
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = story.coverImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 100f
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
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "✨ FEATURED",
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            if (story.isPremium) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFFFD700),
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Star, contentDescription = "Premium", tint = Color(0xFF5D4037), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = story.title,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱️ ${story.readingTime}",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "📖 ${story.pages.size} Pages",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
