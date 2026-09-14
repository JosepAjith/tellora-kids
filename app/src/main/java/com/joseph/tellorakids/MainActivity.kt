package com.joseph.tellorakids

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.joseph.tellorakids.common.managers.UpdateManager
import com.joseph.tellorakids.common.managers.UpdateStatus
import com.joseph.tellorakids.common.utils.AdsManager
import com.joseph.tellorakids.common.utils.BillingManager
import com.joseph.tellorakids.data.datasource.LocalPreferencesDataSource
import com.joseph.tellorakids.ui.navigation.TelloraKidsNavigation
import com.joseph.tellorakids.ui.theme.TelloraKidsTheme
import com.joseph.tellorakids.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adsManager: AdsManager
    @Inject lateinit var billingManager: BillingManager
    @Inject lateinit var updateManager: UpdateManager
    @Inject lateinit var preferences: LocalPreferencesDataSource

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        adsManager.initialize(this)
        
        // Track app opens for Ads/Review capping
        lifecycleScope.launch {
            preferences.incrementAppOpenCount()
        }

        // Check for updates on startup
        updateManager.checkForUpdates()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()
            val windowSizeClass = calculateWindowSizeClass(this)
            val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
            
            val updateStatus by updateManager.updateStatus.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            
            TelloraKidsTheme(darkTheme = settingsState.isDarkMode) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { _ -> 
                    val navController = rememberNavController()
                    
                    // Handle Update Status UI
                    LaunchedEffect(updateStatus) {
                        when (updateStatus) {
                            is UpdateStatus.UpdateAvailable -> {
                                updateManager.startUpdate(this@MainActivity)
                            }
                            UpdateStatus.ReadyToInstall -> {
                                val result = snackbarHostState.showSnackbar(
                                    message = "A new version is ready!",
                                    actionLabel = "Restart",
                                    duration = SnackbarDuration.Indefinite
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    updateManager.completeUpdate()
                                }
                            }
                            else -> {}
                        }
                    }

                    TelloraKidsNavigation(
                        navController = navController,
                        isExpanded = isExpanded
                    )
                }
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        updateManager.onActivityResult(requestCode, resultCode)
    }

    override fun onDestroy() {
        updateManager.unregisterListener()
        super.onDestroy()
    }
}
