package com.joseph.tellorakids.ui.screens.premium

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.joseph.tellorakids.ui.theme.FantasyGradient
import com.joseph.tellorakids.viewmodel.PremiumViewModel

@Composable
fun PremiumScreen(
    onBackClick: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(FantasyGradient))
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unlock Premium",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(32.dp))

            PremiumFeatureItem("100 Magical Stories")
            PremiumFeatureItem("No Interruption Ads")
            PremiumFeatureItem("Future Premium Stories")
            PremiumFeatureItem("Lifetime Access")

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.buyNow(activity) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (uiState.price.isNotEmpty()) "Buy Now - ${uiState.price}" else "Buy Now",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { viewModel.restorePurchase() }) {
                Text("Restore Purchase", color = Color.White.copy(alpha = 0.8f))
            }

            TextButton(onClick = onBackClick) {
                Text("Maybe Later", color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun PremiumFeatureItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
