package com.joseph.tellorakids.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseph.tellorakids.common.utils.BillingManager
import com.joseph.tellorakids.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PremiumUiState(
    val price: String = "",
    val isPremium: Boolean = false
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val repository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                billingManager.premiumPrice,
                repository.isPremiumUser()
            ) { price, isPremium ->
                PremiumUiState(price = price, isPremium = isPremium)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun buyNow(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }

    fun restorePurchase() {
        billingManager.restorePurchases()
    }
}
