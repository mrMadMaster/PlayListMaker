package com.example.playlistmaker.search.domain.interactor.impl

import com.example.playlistmaker.search.domain.interactor.TrackInteractor
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.domain.repository.TrackRepository
import java.util.concurrent.Executors

class TrackInteractorImpl(
    private val trackRepository: TrackRepository
) : TrackInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun searchTracks(searchQuery: String, consumer: TrackInteractor.TrackSearchConsumer) {
        executor.execute {
            try {
                val foundTracks = trackRepository.searchTracks(searchQuery)
                consumer.onTracksFound(foundTracks)
            } catch (exception: Exception) {
                consumer.onSearchError(exception)
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