package com.joseph.tellorakids.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.joseph.tellorakids.data.local.dao.StoryDao
import com.joseph.tellorakids.data.local.entity.StoryEntity
import com.joseph.tellorakids.data.local.entity.StoryPageEntity

@Database(
    entities = [StoryEntity::class, StoryPageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
}
