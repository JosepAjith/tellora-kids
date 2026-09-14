package com.joseph.tellorakids.ui.screens.story

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.joseph.tellorakids.R
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.model.StoryPage
import com.joseph.tellorakids.ui.components.ErrorView
import com.joseph.tellorakids.viewmodel.SettingsViewModel
import com.joseph.tellorakids.viewmodel.StoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoryScreen(
    onBackClick: () -> Unit,
    viewModel: StoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val pageCount = (uiState.story?.pages?.size ?: 0) + 1
    val pagerState = rememberPagerState(pageCount = { pageCount })

    val isDark = settingsState.isDarkMode
    val bodyFontSize = settingsState.fontSize.sp

    // Handle System Back Button
    BackHandler {
        onBackClick()
    }

    // Theme Colors based on prompt
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFDF7)
    val accentColor = Color(0xFFFF6F00) // Orange accent

    // Auto-paging logic (Disabled for current version)
    /*
    LaunchedEffect(Unit) {
        viewModel.speechFinishedEvent.collect {
            if (uiState.autoPlayEnabled && pagerState.currentPage < pagerState.pageCount - 1) {
                delay(1500) // Wait a bit after speech finishes
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        }
    }
    */

    // Save reading progress
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage < (uiState.story?.pages?.size ?: 0)) {
            viewModel.updateCurrentPage(pagerState.currentPage + 1)
        } else if (pagerState.currentPage == uiState.story?.pages?.size) {
            // Moral page is considered completed
            viewModel.updateCurrentPage(uiState.story?.pages?.size ?: 1)
            // Record story read for In-App Review criteria
            viewModel.markStoryAsRead()
        }
    }

    // Start reading automatically (Disabled for current version)
    /*
    LaunchedEffect(pagerState.currentPage, uiState.autoPlayEnabled) {
        if (uiState.autoPlayEnabled) {
            val story = uiState.story
            if (story != null && pagerState.currentPage < story.pages.size) {
                // Only trigger if not already speaking (to prevent loop on autoPlayEnabled change)
                if (!uiState.isSpeaking) {
                    viewModel.toggleSpeak(story.pages[pagerState.currentPage].text)
                }
            } else if (story != null && pagerState.currentPage == story.pages.size && !uiState.isSpeaking) {
                viewModel.toggleSpeak("The moral of the story is: ${story.moral}")
            }
        }
    }
    */

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = bgColor
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                LoadingView(isDark)
            } else if (uiState.error != null) {
                ErrorView(
                    message = uiState.error,
                    onRetry = { viewModel.retry() }
                )
            } else {
                uiState.story?.let { story ->
                    StoryContent(
                        story = story,
                        pagerState = pagerState,
                        uiState = uiState,
                        isDark = isDark,
                        bodyFontSize = bodyFontSize,
                        bgColor = bgColor,
                        accentColor = accentColor,
                        onListenClick = { text -> viewModel.toggleSpeak(text) },
                        onToggleAutoPlay = { /* handled by vm/uiState */ }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                    ) {
                        TopBar(
                            currentPage = pagerState.currentPage + 1,
                            totalPages = pageCount,
                            isFavorite = uiState.isFavorite,
                            onBackClick = onBackClick,
                            onFavoriteClick = { viewModel.toggleFavorite() },
                            isDark = isDark,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        BottomControls(
                            pagerState = pagerState,
                            autoPlayEnabled = uiState.autoPlayEnabled,
                            onAutoPlayToggle = { viewModel.toggleAutoPlay() },
                            isDark = isDark,
                            accentColor = accentColor,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoryContent(
    story: Story,
    pagerState: PagerState,
    uiState: com.joseph.tellorakids.viewmodel.StoryUiState,
    isDark: Boolean,
    bodyFontSize: androidx.compose.ui.unit.TextUnit,
    bgColor: Color,
    accentColor: Color,
    onListenClick: (String) -> Unit,
    onToggleAutoPlay: () -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = !uiState.isSpeaking,
        beyondBoundsPageCount = 1
    ) { pageIndex ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 3D Page Turn Effect
                    val pageOffset = (
                            (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                            ).coerceIn(-1f, 1f)
                    val absOffset = abs(pageOffset)
                    
                    rotationY = -45f * pageOffset
                    val scale = lerp(0.85f, 1f, 1f - absOffset)
                    scaleX = scale
                    scaleY = scale
                    alpha = lerp(0.5f, 1f, 1f - absOffset)
                    cameraDistance = 16f * density
                }
        ) {
            if (pageIndex < story.pages.size) {
                StoryPageItem(
                    page = story.pages[pageIndex],
                    isSpeaking = uiState.isSpeaking && pagerState.currentPage == pageIndex,
                    fontSize = bodyFontSize,
                    isDark = isDark,
                    accentColor = accentColor,
                    onListenClick = { onListenClick(story.pages[pageIndex].text) },
                    pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                )
            } else {
                MoralPageItem(
                    moral = story.moral,
                    title = story.title,
                    coverImage = story.coverImageUrl.ifBlank { story.coverImage },
                    isDark = isDark,
                    accentColor = accentColor,
                    pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                )
            }
        }
    }
}

@Composable
fun StoryPageItem(
    page: StoryPage,
    isSpeaking: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    isDark: Boolean,
    accentColor: Color,
    onListenClick: () -> Unit,
    pageOffset: Float
) {
    val absOffset = abs(pageOffset)
    Box(modifier = Modifier.fillMaxSize()) {
        // Hero Illustration - Using AspectRatio for consistency
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f) // Fixed 3:4 Height
                .graphicsLayer {
                    alpha = 1f - absOffset.coerceIn(0f, 1f)
                    scaleX = 1f + absOffset * 0.1f
                    scaleY = 1f + absOffset * 0.1f
                }
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
        ) {
            val pageImageUrl = page.imageUrl.ifBlank { page.image }
            StoryImage(imageUrl = pageImageUrl, isDark = isDark)
        }

        // Floating Story Card
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 140.dp) // Increased padding to ensure gap with bottom controls
                .graphicsLayer {
                    alpha = (1f - absOffset * 1.5f).coerceIn(0f, 1f)
                    translationY = absOffset * 200f
                    scaleX = 0.9f + (1f - 0.9f) * (1f - absOffset)
                }
        ) {
            StoryCard(
                text = page.text,
                isSpeaking = isSpeaking,
                fontSize = fontSize,
                isDark = isDark,
                accentColor = accentColor,
                onListenClick = onListenClick
            )
        }
    }
}

@Composable
fun StoryImage(imageUrl: String, isDark: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val finalUrl = if (imageUrl.contains("?")) "$imageUrl&t=${System.currentTimeMillis()}" else "$imageUrl?t=${System.currentTimeMillis()}"
        AsyncImage(
            model = finalUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            alignment = Alignment.TopCenter, // Crucial: Keeps heads/faces visible
            onError = { error ->
                android.util.Log.e("IMAGE_LOAD", "Failed to load page image: $finalUrl", error.result.throwable)
            }
        )
        
        // Subtle black overlay for dark mode to maintain contrast if needed
        if (isDark) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.08f))
            )
        }
        
        // Bottom gradient for smoother transition to card area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f))
                    )
                )
        )
    }
}

@Composable
fun StoryCard(
    text: String,
    isSpeaking: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    isDark: Boolean,
    accentColor: Color,
    onListenClick: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color(0xFFFFFDF7)
    val textColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF3E2723)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = if (isDark) Color.Black else accentColor.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = if (isDark) BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sparkle Decoration (Optional/Visual)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SparkleIcon(accentColor)
                Spacer(modifier = Modifier.weight(1f))
                SparkleIcon(accentColor)
            }

            StoryText(
                text = text,
                isSpeaking = isSpeaking,
                fontSize = fontSize,
                textColor = textColor,
                accentColor = accentColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Divider Decoration
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(1.5.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, accentColor.copy(alpha = 0.3f))
                            ),
                            shape = CircleShape
                        )
                )
                Icon(
                    imageVector = Icons.Rounded.Eco,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp).padding(horizontal = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(1.5.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(accentColor.copy(alpha = 0.3f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
            }

            // Spacer(modifier = Modifier.height(20.dp))

            // ListenButton hidden for current version
            /*
            ListenButton(
                isSpeaking = isSpeaking,
                onListenClick = onListenClick,
                isDark = isDark,
                accentColor = accentColor
            )
            */
        }
    }
}

@Composable
fun StoryText(
    text: String,
    isSpeaking: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    textColor: Color,
    accentColor: Color
) {
    // Highlight important words (simplified: just making it look good)
    val annotatedContent = buildAnnotatedString {
        val words = text.split(" ")
        words.forEachIndexed { index, word ->
            val isHighlighted = isSpeaking && index % 4 == 0 // Mocking word highlight
            withStyle(
                style = SpanStyle(
                    color = if (isHighlighted) accentColor else textColor,
                    fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Bold
                )
            ) {
                append(word)
            }
            if (index < words.size - 1) append(" ")
        }
    }

    Text(
        text = annotatedContent,
        style = TextStyle(
            fontSize = fontSize * 1.2f,
            lineHeight = (fontSize.value * 1.8).sp,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.SansSerif // Ideally a rounded font
        ),
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
fun ListenButton(
    isSpeaking: Boolean,
    onListenClick: () -> Unit,
    isDark: Boolean,
    accentColor: Color
) {
    Button(
        onClick = onListenClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSpeaking) accentColor.copy(alpha = 0.1f) else accentColor,
            contentColor = if (isSpeaking) accentColor else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(48.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isSpeaking) Icons.Rounded.Stop else Icons.Rounded.VolumeUp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSpeaking) "Stop" else "Listen",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun TopBar(
    currentPage: Int,
    totalPages: Int,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                    CircleShape
                )
        ) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        // Page Indicator pill
        Surface(
            color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📖 $currentPage / $totalPages",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Favorite Button
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color(0xFFFF4081) else Color.White
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomControls(
    pagerState: PagerState,
    autoPlayEnabled: Boolean,
    onAutoPlayToggle: () -> Unit,
    isDark: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val controlBg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page Indicator Dots
            Row(
                modifier = Modifier
                    .background(controlBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val active = pagerState.currentPage == iteration
                    val width by animateDpAsState(targetValue = if (active) 20.dp else 8.dp, label = "dotWidth")
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(CircleShape)
                            .background(if (active) accentColor else if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.2f))
                            .size(height = 8.dp, width = width)
                    )
                }
            }
        }
    }
}

@Composable
fun MoralPageItem(
    moral: String,
    title: String,
    coverImage: String,
    isDark: Boolean,
    accentColor: Color,
    pageOffset: Float
) {
    val absOffset = abs(pageOffset)
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.magic_confetti))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    val textColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF3E2723)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 1f - absOffset
                scaleX = 0.9f + (1f - 0.9f) * (1f - absOffset)
            },
        contentAlignment = Alignment.Center
    ) {
        // Background illustration (Faded)
        val finalUrl = if (coverImage.contains("?")) "$coverImage&t=${System.currentTimeMillis()}" else "$coverImage?t=${System.currentTimeMillis()}"
        AsyncImage(
            model = finalUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            alpha = if (isDark) 0.15f else 0.2f,
            onError = { error ->
                android.util.Log.e("IMAGE_LOAD", "Failed to load background image: $finalUrl", error.result.throwable)
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(320.dp)
                )
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(120.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🌟", fontSize = 60.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "The Magic Lesson",
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Moral Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFF9C4).copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = moral,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = if (isDark) Color.White else Color(0xFF5D4037),
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp,
                        fontFamily = FontFamily.Serif
                    ),
                    modifier = Modifier.padding(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "THE END",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = accentColor,
                letterSpacing = 8.sp
            )
        }
    }
}

@Composable
fun SparkleIcon(color: Color) {
    Icon(
        Icons.Rounded.AutoAwesome,
        contentDescription = null,
        tint = color.copy(alpha = 0.4f),
        modifier = Modifier.size(16.dp)
    )
}

@Composable
fun LoadingView(isDark: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = Color(0xFFFF6F00),
                strokeWidth = 6.dp,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Opening the magic book...",
                color = if (isDark) Color.White else Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

