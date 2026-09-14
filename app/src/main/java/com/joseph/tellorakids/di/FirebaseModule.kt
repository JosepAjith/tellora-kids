package com.joseph.tellorakids.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    // Analytics is handled automatically by the plugin/google-services.json
    // If explicit injection is needed later, add it here.
}
