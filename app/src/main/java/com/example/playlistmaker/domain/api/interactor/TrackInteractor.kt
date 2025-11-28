package com.example.playlistmaker.domain.api.interactor

import com.example.playlistmaker.domain.models.Track

interface TrackInteractor {
    fun searchTracks(searchQuery: String, consumer: TrackSearchConsumer)

    fun getSearchHistory(): List<Track>
    fun addToSearchHistory(track: Track)
    fun clearSearchHistory()

    interface TrackSearchConsumer {
        fun onTracksFound(foundTracks: List<Track>)
        fun onSearchError(exception: Exception)
    }
}