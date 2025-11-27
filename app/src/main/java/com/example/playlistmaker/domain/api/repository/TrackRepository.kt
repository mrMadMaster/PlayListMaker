package com.example.playlistmaker.domain.api.repository

import com.example.playlistmaker.domain.models.Track

interface TrackRepository {
    fun searchTracks(expression: String): List<Track>

    fun getSearchHistory(): List<Track>
    fun addToSearchHistory(track: Track)
    fun clearSearchHistory()
}