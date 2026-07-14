package com.joseph.tellorakids.common.utils

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManager @Inject constructor() {
    
    companion object {
        const val HOME_BANNER_ID = "ca-app-pub-3940256099942544/6300978111" // Test ID
        const val LIST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111" // Test ID
    }

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
}
