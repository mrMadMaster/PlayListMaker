package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.api.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.models.Track

class SearchHistoryInteractorImpl (private val repository: SearchHistoryRepository):
    SearchHistoryInteractor {

    override fun readHistory(): List<Track> {
        return repository.readHistory()
    }

    override fun writeHistory(track: Track) {
        repository.writeHistory(track)
    }

    override fun clearHistory() {
        repository.clearHistory()
    }
}