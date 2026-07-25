package com.joseph.tellorakids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.joseph.tellorakids.common.utils.AdsManager
import com.joseph.tellorakids.common.utils.BillingManager
import com.joseph.tellorakids.ui.navigation.TelloraKidsNavigation
import com.joseph.tellorakids.ui.theme.TelloraKidsTheme
import com.joseph.tellorakids.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.json.Json
import com.joseph.tellorakids.domain.model.Story
import kotlinx.serialization.decodeFromString
import android.util.Log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adsManager: AdsManager
    @Inject lateinit var billingManager: BillingManager

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        adsManager.initialize(this)

        // TODO: Uncomment to seed database, then delete after one successful run
        seedDatabase()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()
            val windowSizeClass = calculateWindowSizeClass(this)
            val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
            
            TelloraKidsTheme(darkTheme = settingsState.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    TelloraKidsNavigation(
                        navController = navController,
                        isExpanded = isExpanded
                    )
                }
            }
        }
    }

    private fun seedDatabase() {
        val firestore = FirebaseFirestore.getInstance()
        
        // PASTE YOUR JSON CONTENT BETWEEN THE TRIPLE QUOTES BELOW
        val jsonString = """
  [
     {
       "id": "story_0011",
       "version": 1,
       "title": "A Little Leaf's Journey",
       "category": "Nature",
       "coverImage": "",
       "ageGroup": "3-5",
       "readingTime": "2 min",
       "language": "en",
       "pages": [
         {
           "page": 1,
           "image": "",
           "text": "A little leaf danced on a tree. \"I want to see the world!\" said the leaf."
         },
         {
           "page": 2,
           "image": "",
           "text": "The wind gently carried the leaf over a beautiful garden. \"Wow! Everything looks so wonderful!\" said the little leaf."
         },
         {
           "page": 3,
           "image": "",
           "text": "The leaf met a colorful butterfly and a tiny bird. \"Come with us,\" they said. \"Let's explore together!\""
         },
         {
           "page": 4,
           "image": "",
           "text": "Soon, the leaf landed near its tree in a warm, sunny spot. \"What a wonderful adventure!\" smiled the leaf."
         }
       ],
       "moral": "",
       "isFeatured": true,
       "isPremium": false
     }]
        """.trimIndent()

        if (jsonString == "[]") {
            Log.w("SEEDER", "No JSON data found. Please paste your JSON into the jsonString variable.")
            return
        }

        try {
            val stories = Json { 
                ignoreUnknownKeys = true 
                coerceInputValues = true
            }.decodeFromString<List<Story>>(jsonString)
            
            stories.forEach { story ->
                firestore.collection("stories")
                    .document(story.id)
                    .set(story)
                    .addOnSuccessListener { 
                        Log.d("SEEDER", "✅ Successfully uploaded: ${story.title}") 
                    }
                    .addOnFailureListener { e -> 
                        Log.e("SEEDER", "❌ Failed to upload: ${story.title}", e) 
                    }
            }
        } catch (e: Exception) {
            Log.e("SEEDER", "💥 Error parsing JSON: ${e.message}", e)
        }
    }
}
