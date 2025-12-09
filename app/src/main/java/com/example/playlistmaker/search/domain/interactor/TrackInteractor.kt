package com.example.playlistmaker.search.domain.interactor

import com.example.playlistmaker.search.domain.models.Track

interface TrackInteractor {
    fun searchTracks(searchQuery: String, consumer: TrackSearchConsumer)

    interface TrackSearchConsumer {
        fun onTracksFound(foundTracks: List<Track>)
        fun onSearchError(exception: Exception)
    }

    fun getSearchHistory(): List<Track>
    fun addToSearchHistory(track: Track)
    fun clearSearchHistory()

}