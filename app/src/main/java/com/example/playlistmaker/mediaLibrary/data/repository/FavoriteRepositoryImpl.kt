package com.example.playlistmaker.mediaLibrary.data.repository

import com.example.playlistmaker.mediaLibrary.data.db.dao.FavoriteTrackDao
import com.example.playlistmaker.mediaLibrary.data.db.entity.FavoriteTrackEntity
import com.example.playlistmaker.mediaLibrary.domain.repository.FavoriteRepository
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FavoriteRepositoryImpl(
    private val favoriteTrackDao: FavoriteTrackDao
) : FavoriteRepository {

    override suspend fun addToFavorites(track: Track) {
        favoriteTrackDao.insertTrack(
            FavoriteTrackEntity(
                trackId = track.trackId,
                trackName = track.trackName,
                artistName = track.artistName,
                trackTimeMillis = track.trackTimeMillis,
                artworkUrl100 = track.artworkUrl100,
                previewUrl = track.previewUrl,
                collectionName = track.collectionName,
                releaseDate = track.releaseDate,
                primaryGenreName = track.primaryGenreName,
                country = track.country
            )
        )
    }

    override suspend fun removeFromFavorites(track: Track) {
        favoriteTrackDao.deleteTrack(
            FavoriteTrackEntity(
                trackId = track.trackId,
                trackName = track.trackName,
                artistName = track.artistName,
                trackTimeMillis = track.trackTimeMillis,
                artworkUrl100 = track.artworkUrl100,
                previewUrl = track.previewUrl,
                collectionName = track.collectionName,
                releaseDate = track.releaseDate,
                primaryGenreName = track.primaryGenreName,
                country = track.country
            )
        )
    }

    override fun getFavorites(): Flow<List<Track>> {
        return favoriteTrackDao.getAllTracks()
            .distinctUntilChanged()
            .map { entities ->
                entities.map { entity ->
                    Track(
                        trackId = entity.trackId,
                        trackName = entity.trackName,
                        artistName = entity.artistName,
                        trackTimeMillis = entity.trackTimeMillis,
                        artworkUrl100 = entity.artworkUrl100,
                        previewUrl = entity.previewUrl,
                        collectionName = entity.collectionName,
                        releaseDate = entity.releaseDate,
                        primaryGenreName = entity.primaryGenreName,
                        country = entity.country
                    ).apply { isFavorite = true }
                }
            }
    }

    override suspend fun getFavoriteIds(): List<Int> {
        return favoriteTrackDao.getAllFavoriteIds()
    }
}