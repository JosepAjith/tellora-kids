package com.joseph.tellorakids.ui.screens.story

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.joseph.tellorakids.domain.model.StoryPage
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import com.joseph.tellorakids.viewmodel.StoryViewModel
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StoryScreen(
    onBackClick: () -> Unit,
    viewModel: StoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = {
        (uiState.story?.pages?.size ?: 0) + 1
    })

    // Auto-paging logic when speech finishes
    LaunchedEffect(Unit) {
        viewModel.speechFinishedEvent.collect {
            if (uiState.autoPlayEnabled && pagerState.currentPage < pagerState.pageCount - 1) {
                delay(1000)
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        }
    }

    // Start reading automatically if autoPlay is on when page changes
    LaunchedEffect(pagerState.currentPage) {
        if (uiState.autoPlayEnabled) {
            val story = uiState.story
            if (story != null && pagerState.currentPage < story.pages.size) {
                viewModel.toggleSpeak(story.pages[pagerState.currentPage].text)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color(0xFFFFF8EE) // Warm paper background
    ) { padding ->
        uiState.story?.let { story ->
            Box(modifier = Modifier.fillMaxSize()) {

                // 1. The Main Story Content with 3D Page Effect
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = !uiState.isSpeaking,
                    beyondBoundsPageCount = 1
                ) { pageIndex ->
                    val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // 3D Page Turn Effect
                                val absOffset = abs(pageOffset)
                                
                                // Slight 3D rotation
                                rotationY = -45f * pageOffset
                                
                                // Scale animation
                                val scale = 0.85f + (1f - 0.85f) * (1f - absOffset.coerceIn(0f, 1f))
                                scaleX = scale
                                scaleY = scale
                                
                                // Alpha transition
                                alpha = 1f - absOffset.coerceIn(0f, 1f) * 0.5f
                                
                                // Depth effect
                                cameraDistance = 16f * density
                                
                                // Translation for smooth page curl feeling
                                translationX = pageOffset * size.width * 0.1f
                                
                                // Page shadow
                                shadowElevation = (1f - absOffset) * 8f
                            }
                            .background(Color(0xFFFFF8EE))
                    ) {
                        if (pageIndex < story.pages.size) {
                            StoryPageContent(
                                page = story.pages[pageIndex],
                                isSpeaking = uiState.isSpeaking && pagerState.currentPage == pageIndex,
                                pageOffset = pageOffset
                            )
                        } else {
                            MoralPageContent(
                                moral = story.moral,
                                title = story.title,
                                coverImage = story.coverImage,
                                pageOffset = pageOffset
                            )
                        }
                    }
                }

                // 2. Top Bar (Transparent over image)
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (uiState.isFavorite) Color(0xFFFF4081) else Color.White
                        )
                    }
                }

                // 3. Page Indicator
                Row(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 110.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val active = pagerState.currentPage == iteration
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(CircleShape)
                                .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                .size(if (active) 10.dp else 8.dp)
                        )
                    }
                }

                // 4. Bottom Pill Button
                ReadingPillButton(
                    isSpeaking = uiState.isSpeaking,
                    onToggleSpeak = {
                        if (pagerState.currentPage < story.pages.size) {
                            viewModel.toggleSpeak(story.pages[pagerState.currentPage].text)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp)
                        .navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
fun ReadingPillButton(
    isSpeaking: Boolean,
    onToggleSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onToggleSpeak,
        modifier = modifier
            .height(60.dp)
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isSpeaking) "⏹" else "🔊",
                fontSize = 22.sp
            )
            Text(
                text = if (isSpeaking) "Stop Reading" else "Read Story",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun StoryPageContent(
    page: StoryPage,
    isSpeaking: Boolean,
    pageOffset: Float
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp > 600

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Hero Image: Occupies most of the screen, adjusted for aspect ratio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isLandscape) 1f else 1.8f) // Higher weight on portrait for "Hero" feel
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(Color(0xFFEFEBE9))
        ) {
            AsyncImage(
                model = page.image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // THE STORYTELLING PAN:
                        // Since your images are 16:9 (wide) and phones are tall, 
                        // we use a "Parallax Pan". As you swipe the page, the image 
                        // slides horizontally to reveal the hidden parts of the scene.
                        // This prevents important story details from being permanently cropped.
                        val panRange = 300f 
                        translationX = pageOffset * panRange
                        
                        // Zoom slightly to provide "bleed" for the panning effect
                        scaleX = 1.2f
                        scaleY = 1.2f
                    },
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
            
            // Beautiful gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f)
                            ),
                            startY = 0.7f
                        )
                    )
            )
        }

        // Story Text Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val textStyle = TextStyle(
                fontSize = if (isTablet) 32.sp else 24.sp,
                lineHeight = if (isTablet) 48.sp else 38.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF3E2723)
            )

            if (isSpeaking) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(
                            background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )) {
                            append(page.text)
                        }
                    },
                    style = textStyle
                )
            } else {
                Text(
                    text = page.text,
                    style = textStyle
                )
            }
        }
    }
}

@Composable
fun MoralPageContent(
    moral: String,
    title: String,
    coverImage: String,
    pageOffset: Float
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.joseph.tellorakids.R.raw.magic_confetti))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background illustration
        AsyncImage(
            model = coverImage,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.1f
                    scaleY = 1.1f
                    translationX = pageOffset * 100f
                },
            contentScale = ContentScale.Crop,
            alpha = 0.15f
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
                    modifier = Modifier.size(300.dp)
                )
                Text(text = "🌟", fontSize = 80.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "The Magic Lesson of",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF3E2723),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = moral,
                style = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color(0xFF5D4037),
                    textAlign = TextAlign.Center,
                    lineHeight = 38.sp,
                    fontFamily = FontFamily.Serif
                )
            )

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "THE END",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 6.sp
            )
        }
    }
}
