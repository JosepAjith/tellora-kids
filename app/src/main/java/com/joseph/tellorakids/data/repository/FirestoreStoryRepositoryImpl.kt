package com.joseph.tellorakids.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.joseph.tellorakids.data.datasource.LocalPreferencesDataSource
import com.joseph.tellorakids.domain.model.Story
import com.joseph.tellorakids.domain.repository.StoryRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreStoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val preferencesDataSource: LocalPreferencesDataSource
) : StoryRepository {

    private val storiesCollection = firestore.collection("stories")

    override fun getAllStories(ageGroup: String): Flow<List<Story>> = callbackFlow {
        Log.d("FIRESTORE", "Fetching all stories for ageGroup: $ageGroup")
        val query = if (ageGroup == "all") {
            storiesCollection
        } else {
            storiesCollection.whereEqualTo("ageGroup", ageGroup)
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FIRESTORE", "Error fetching stories", error)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val stories = snapshot.toObjects(Story::class.java)
                Log.d("FIRESTORE", "Successfully fetched ${stories.size} stories")
                stories.forEach { 
                    Log.d("FIRESTORE", "Story: ${it.title}, Image URL: ${it.coverImage}")
                }
                trySend(stories)
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getStoriesByCategory(category: String, ageGroup: String): Flow<List<Story>> = callbackFlow {
        var query = storiesCollection.whereEqualTo("category", category)
        if (ageGroup != "all") {
            query = query.whereEqualTo("ageGroup", ageGroup)
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {2
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val stories = snapshot.toObjects(Story::class.java)
                trySend(stories)
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getStoryById(id: String): Flow<Story?> = callbackFlow {
        val subscription = storiesCollection.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Story::class.java))
        }
        awaitClose { subscription.remove() }
    }

    override fun getFeaturedStories(ageGroup: String): Flow<List<Story>> = callbackFlow {
        var query = storiesCollection.whereEqualTo("featured", true)
        if (ageGroup != "all") {
            query = query.whereEqualTo("ageGroup", ageGroup)
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val stories = snapshot.toObjects(Story::class.java)
                trySend(stories)
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun searchStories(query: String, ageGroup: String): Flow<List<Story>> = flow {
        val snapshot = if (ageGroup == "all") {
            storiesCollection.get().await()
        } else {
            storiesCollection.whereEqualTo("ageGroup", ageGroup).get().await()
        }
        
        val stories = snapshot.toObjects(Story::class.java)
        val filtered = stories.filter {
            it.title.contains(query, ignoreCase = true) || 
            it.category.contains(query, ignoreCase = true)
        }
        emit(filtered)
    }

    // Favorites
    override fun getFavoriteStoryIds(): Flow<Set<String>> = preferencesDataSource.favoriteStoryIds
    override suspend fun toggleFavorite(storyId: String) = preferencesDataSource.toggleFavorite(storyId)

    // Continue Reading
    override fun getRecentStoryId(): Flow<String?> = preferencesDataSource.recentStoryId
    override suspend fun saveRecentStory(storyId: String) = preferencesDataSource.saveRecentStory(storyId)
    override fun getStoryProgress(storyId: String): Flow<Int> = preferencesDataSource.getStoryProgress(storyId)
    override suspend fun saveStoryProgress(storyId: String, page: Int) = preferencesDataSource.saveStoryProgress(storyId, page)

    // Age Filter
    override fun getSelectedAgeGroup(): Flow<String> = preferencesDataSource.selectedAgeGroup
    override suspend fun setSelectedAgeGroup(ageGroup: String) = preferencesDataSource.setSelectedAgeGroup(ageGroup)

    // Premium
    override fun isPremiumUser(): Flow<Boolean> = preferencesDataSource.isPremium
    override suspend fun setPremiumStatus(isPremium: Boolean) = preferencesDataSource.setPremiumStatus(isPremium)
}
