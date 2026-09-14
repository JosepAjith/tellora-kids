package com.joseph.tellorakids.viewmodel

import androidx.lifecycle.ViewModel
import com.joseph.tellorakids.common.utils.AdsManager
import com.joseph.tellorakids.common.managers.ReviewManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdsWrapperViewModel @Inject constructor(
    val adsManager: AdsManager,
    val reviewManager: ReviewManager
) : ViewModel()
