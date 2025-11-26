package com.example.playlistmaker.domain.api.interactor

import com.example.playlistmaker.domain.models.Track

interface SearchHistoryInteractor {
    fun readHistory(): List<Track>
    fun writeHistory(track: Track)
    fun clearHistory()
}