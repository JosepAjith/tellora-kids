package com.joseph.tellorakids.common.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.android.billingclient.api.*
import com.joseph.tellorakids.domain.repository.StoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: StoryRepository
) : PurchasesUpdatedListener {

    private val _premiumPrice = MutableStateFlow("")
    val premiumPrice = _premiumPrice.asStateFlow()

    private var isBillingClientReady = false

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isBillingClientReady = true
                    queryProductDetails()
                    restorePurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                isBillingClientReady = false
                startConnection()
            }
        })
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium_upgrade")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = productDetailsList.find { it.productId == "premium_upgrade" }
                _premiumPrice.value = details?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$4.99" // Default display price if Play Store fails
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        if (!isBillingClientReady) {
            // DEBUG BYPASS: If Google Play is not connected (common in dev), simulate purchase
            // Remove this in real production upload
            Toast.makeText(context, "Debug: Simulating Premium Activation", Toast.LENGTH_SHORT).show()
            setPremiumStatus(true)
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium_upgrade")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetails = productDetailsList.find { it.productId == "premium_upgrade" }
                if (productDetails != null) {
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                    val flowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()
                    billingClient.launchBillingFlow(activity, flowParams)
                } else {
                    // Simulating purchase if product not found in local Play Store (common for dev builds not on Console)
                    setPremiumStatus(true)
                    Toast.makeText(context, "Premium Unlocked (Simulation)", Toast.LENGTH_SHORT).show()
                }
            } else {
                setPremiumStatus(true)
                Toast.makeText(context, "Premium Unlocked (Simulation)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        setPremiumStatus(true)
                    }
                }
            } else {
                setPremiumStatus(true)
            }
        }
    }

    fun restorePurchases() {
        if (!isBillingClientReady) return

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            purchase.products.contains("premium_upgrade")
                }
                setPremiumStatus(hasPremium)
            }
        }
    }

    private fun setPremiumStatus(isPremium: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.setPremiumStatus(isPremium)
        }
    }
}
