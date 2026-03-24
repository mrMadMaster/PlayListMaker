package com.example.playlistmaker.mediaLibrary.domain.interactor.impl

import com.example.playlistmaker.mediaLibrary.domain.interactor.FavoriteInteractor
import com.example.playlistmaker.mediaLibrary.domain.repository.FavoriteRepository
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

class FavoriteInteractorImpl(
    private val favoriteRepository: FavoriteRepository
) : FavoriteInteractor {
    override suspend fun addToFavorites(track: Track) {
        favoriteRepository.addToFavorites(track)
    }

    override suspend fun removeFromFavorites(track: Track) {
        favoriteRepository.removeFromFavorites(track)
    }

    override fun getFavorites(): Flow<List<Track>> {
        return favoriteRepository.getFavorites()
    }

    override suspend fun getFavoriteIds(): List<Int> {
        return favoriteRepository.getFavoriteIds()
    }

    override suspend fun isFavorite(trackId: Int): Boolean {
        return favoriteRepository.isFavorite(trackId)
    }
}