package com.example.playlistmaker.search.domain.interactor

import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface TrackInteractor {
    fun searchTracks(query: String): Flow<List<Track>>

    fun getSearchHistory(): Flow<List<Track>>
    suspend fun addToSearchHistory(track: Track)
    suspend fun clearSearchHistory()
}