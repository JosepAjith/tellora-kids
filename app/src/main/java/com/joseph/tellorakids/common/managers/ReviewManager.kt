package com.joseph.tellorakids.common.managers

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import com.joseph.tellorakids.data.datasource.LocalPreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: LocalPreferencesDataSource
) {
    private val manager = ReviewManagerFactory.create(context)

    /**
     * Increment story count when a story is read.
     */
    suspend fun recordStoryRead() {
        preferences.incrementStoriesReadCount()
    }

    /**
     * Check if user meets criteria for review request.
     */
    suspend fun shouldShowReview(): Boolean {
        val storiesRead = preferences.storiesReadCount.first()
        val appOpens = preferences.appOpenCount.first()
        val uniqueDays = preferences.uniqueDaysUsed.first().size
        val lastReviewDate = preferences.lastReviewDate.first()
        val firstInstallDate = preferences.firstInstallDate.first()

        // Rules:
        // 1. Never on first launch (appOpens > 1)
        // 2. Read at least 5 stories
        // 3. Either used for 3 different days OR opened 10 times
        // 4. At least 90 days since last review request (or never requested)

        val meetsUsageCriteria = storiesRead >= 5 && (uniqueDays >= 3 || appOpens >= 10)
        
        val now = System.currentTimeMillis()
        val daysSinceLastReview = (now - lastReviewDate) / (1000 * 60 * 60 * 24)
        val isCooldownPeriodOver = lastReviewDate == 0L || daysSinceLastReview >= 90

        // Never ask on very first launch/install day if usage criteria not met
        val isFirstLaunch = appOpens <= 1

        return !isFirstLaunch && meetsUsageCriteria && isCooldownPeriodOver
    }

    /**
     * Request and show the review flow if criteria are met.
     */
    suspend fun requestReview(activity: Activity) {
        if (!shouldShowReview()) return

        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // Flow finished (either user reviewed or dismissed)
                    // Record the attempt regardless of outcome to start cooldown
                    CoroutineScope(Dispatchers.IO).launch {
                        preferences.recordReviewRequested()
                    }
                }
            }
        }
    }
}
