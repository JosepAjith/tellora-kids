package com.joseph.tellorakids.di

import android.content.Context
import androidx.room.Room
import com.joseph.tellorakids.data.local.StoryDatabase
import com.joseph.tellorakids.data.local.dao.StoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideStoryDatabase(@ApplicationContext context: Context): StoryDatabase {
        return Room.databaseBuilder(
            context,
            StoryDatabase::class.java,
            "tellora_kids_cache.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideStoryDao(database: StoryDatabase): StoryDao {
        return database.storyDao()
    }
}
