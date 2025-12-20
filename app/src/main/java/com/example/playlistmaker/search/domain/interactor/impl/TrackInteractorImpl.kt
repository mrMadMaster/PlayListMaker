package com.example.playlistmaker.search.domain.interactor.impl

import com.example.playlistmaker.search.domain.interactor.TrackInteractor
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.domain.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackInteractorImpl(
    private val trackRepository: TrackRepository
) : TrackInteractor {

    override suspend fun searchTracks(query: String): List<Track> {
        return withContext(Dispatchers.IO) {
            try {
                trackRepository.searchTracks(query)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override fun getSearchHistory(): List<Track> {
        return trackRepository.getSearchHistory()
    }

    override fun addToSearchHistory(track: Track) {
        trackRepository.addToSearchHistory(track)
    }

    override fun clearSearchHistory() {
        trackRepository.clearSearchHistory()
    }
}