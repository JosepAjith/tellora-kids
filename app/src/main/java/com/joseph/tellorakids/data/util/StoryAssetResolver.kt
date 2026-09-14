package com.joseph.tellorakids.data.util

import com.joseph.tellorakids.BuildConfig

object StoryAssetResolver {
    fun resolve(key: String?): String {
        if (key.isNullOrBlank()) return ""
        if (key.startsWith("http://") || key.startsWith("https://")) return key
        
        val baseUrl = BuildConfig.R2_PUBLIC_BASE_URL.trimEnd('/')
        val sanitizedKey = key.trimStart('/')
        
        return "$baseUrl/$sanitizedKey"
    }
}
