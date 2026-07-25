package com.joseph.tellorakids.di

import com.joseph.tellorakids.data.repository.FirestoreStoryRepositoryImpl
import com.joseph.tellorakids.domain.repository.StoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStoryRepository(
        storyRepositoryImpl: FirestoreStoryRepositoryImpl
    ): StoryRepository
}
