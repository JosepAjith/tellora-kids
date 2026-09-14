package com.joseph.tellorakids.common.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.joseph.tellorakids.data.datasource.LocalPreferencesDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManager @Inject constructor(
    private val preferences: LocalPreferencesDataSource
) {
    
    companion object {
        const val HOME_BANNER_ID = "ca-app-pub-3940256099942544/6300978111" // Test ID
        const val LIST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111" // Test ID
        const val INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712" // Test ID
        
        private const val MIN_TIME_BETWEEN_ADS = 30 * 60 * 1000L // 30 Minutes
        private const val MAX_ADS_PER_DAY = 2
    }

    private var interstitialAd: InterstitialAd? = null

    fun initialize(context: Context) {
        // Configure for child-directed treatment (COPPA / Designed for Families)
        val requestConfiguration = MobileAds.getRequestConfiguration()
            .toBuilder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(context) {}
        }
    }

    /**
     * Load an interstitial ad to have it ready.
     */
    fun loadInterstitial(context: Context) {
        android.util.Log.d("AdsManager", "loadInterstitial called")
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    android.util.Log.d("AdsManager", "onAdLoaded")
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    android.util.Log.e("AdsManager", "onAdFailedToLoad: ${error.message}")
                    interstitialAd = null
                }
            }
        )
    }

    /**
     * Check if we should show an ad based on frequency capping rules.
     */
    private suspend fun canShowAd(): Boolean {
        val lastAdTime = preferences.lastAdTime.first()
        val dailyCount = preferences.dailyAdCount.first()
        val appOpens = preferences.appOpenCount.first()
        
        val now = System.currentTimeMillis()
        val timePassed = now - lastAdTime
        
        // Rules:
        // 1. Never on first launch (appOpens > 1)
        // 2. Max 2 per day
        // 3. At least 30 minutes apart
        
        return appOpens > 1 && dailyCount < MAX_ADS_PER_DAY && timePassed >= MIN_TIME_BETWEEN_ADS
    }

    /**
     * Show the interstitial ad if ready and criteria are met.
     */
    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit = {}) {
        CoroutineScope(Dispatchers.Main).launch {
            val canShow = canShowAd()
            val adReady = interstitialAd != null
            android.util.Log.d("AdsManager", "showInterstitial: canShow=$canShow, adReady=$adReady")
            if (canShow && adReady) {
                interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        onAdDismissed()
                        // Preload next
                        loadInterstitial(activity)
                    }

                    override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                        interstitialAd = null
                        onAdDismissed()
                    }
                }
                
                // Record the show before showing to ensure count is updated
                preferences.recordAdShown()
                interstitialAd?.show(activity)
            } else {
                onAdDismissed()
            }
        }
    }
}
