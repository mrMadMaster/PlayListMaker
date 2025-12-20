package com.example.playlistmaker.search.domain.repository

import com.example.playlistmaker.search.domain.models.Track

interface TrackRepository {
    fun searchTracks(searchQuery: String): List<Track>

    fun getSearchHistory(): List<Track>
    fun addToSearchHistory(track: Track)
    fun clearSearchHistory()
}