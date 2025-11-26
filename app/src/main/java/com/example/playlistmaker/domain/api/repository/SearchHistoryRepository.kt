package com.example.playlistmaker.domain.api.repository

import com.example.playlistmaker.domain.models.Track

interface SearchHistoryRepository {
    fun readHistory(): List<Track>
    fun writeHistory(track: Track)
    fun clearHistory()
}