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
}
