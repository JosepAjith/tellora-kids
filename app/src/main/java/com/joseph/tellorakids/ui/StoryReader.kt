package com.joseph.tellorakids.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Pastel Colors for the Kid-Friendly UI
val SoftPink = Color(0xFFFFD1DC)
val SoftBlue = Color(0xFFAEC6CF)
val SoftGreen = Color(0xFFB2E2D2)
val SoftYellow = Color(0xFFFFF9C4)
val KidsOrange = Color(0xFFFFE0B2)

@Composable
fun StoryReader(
    title: String,
    currentPageText: String,
    imageUrl: String,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftYellow) // Pastel background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Story Title with a playful font style
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF4E342E) // Darker brown for readability
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Main Story Card - High rounded corners
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Illustration Area using Coil
                Box(modifier = Modifier.weight(1.2f)) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Story illustration",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(30.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Optional Lottie Overlay (e.g. for sparkling effects)
                    // StoryAnimation(modifier = Modifier.matchParentSize())
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Page Text - Large and readable
                Text(
                    text = currentPageText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 24.sp,
                        lineHeight = 34.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(horizontal = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Navigation Controls - Large touch targets for kids
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Back" Button
            Button(
                onClick = onPreviousPage,
                modifier = Modifier
                    .height(90.dp)
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Back", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(modifier = Modifier.width(24.dp))

            // "Next" Button
            Button(
                onClick = onNextPage,
                modifier = Modifier
                    .height(90.dp)
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftGreen),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Next", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}
