package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.interactor.TrackInteractor
import com.example.playlistmaker.domain.api.repository.TrackRepository
import java.util.concurrent.Executors

class TrackInteractorImpl(
    private val trackRepository: TrackRepository
) : TrackInteractor {

    private val backgroundExecutor = Executors.newCachedThreadPool()

    override fun searchTracks(searchQuery: String, consumer: TrackInteractor.TrackSearchConsumer) {
        backgroundExecutor.execute {
            try {
                val searchResults = trackRepository.searchTracks(searchQuery)
                consumer.onTracksFound(searchResults)
            } catch (exception: Exception) {
                consumer.onSearchError(exception)
            }
        }
    }
}