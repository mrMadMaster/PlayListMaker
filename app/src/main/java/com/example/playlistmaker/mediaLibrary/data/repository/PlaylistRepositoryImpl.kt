package com.example.playlistmaker.mediaLibrary.data.repository

import com.example.playlistmaker.mediaLibrary.data.db.AppDatabase
import com.example.playlistmaker.mediaLibrary.data.db.entity.PlaylistEntity
import com.example.playlistmaker.mediaLibrary.data.db.entity.PlaylistTrackEntity
import com.example.playlistmaker.mediaLibrary.domain.models.Playlist
import com.example.playlistmaker.mediaLibrary.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val appDatabase: AppDatabase
) : PlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist, coverPath: String?): Long {
        val entity = PlaylistEntity(
            name = playlist.name,
            description = playlist.description,
            coverPath = coverPath
        )
        return appDatabase.playlistDao().insertPlaylist(entity)
    }

    override suspend fun addTrackToPlaylist(playlist: Playlist, track: Track): Boolean {
        val existingTrack = appDatabase.playlistTrackDao()
            .getTrackByPlaylistAndTrack(playlist.id, track.trackId)

        if (existingTrack != null) {
            return false
        }

        appDatabase.playlistTrackDao().insertTrack(
            PlaylistTrackEntity(
                playlistId = playlist.id,
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

        return true
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return appDatabase.playlistDao().getAllPlaylists()
            .map { entities ->
                entities.map { entity ->
                    Playlist(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        coverPath = entity.coverPath,
                        trackCount = 0
                    )
                }
            }
    }

    override suspend fun getPlaylistById(id: Int): Playlist? {
        val entity = appDatabase.playlistDao().getPlaylistById(id) ?: return null
        val trackCount = appDatabase.playlistTrackDao().getTrackCount(id)

        return Playlist(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            coverPath = entity.coverPath,
            trackCount = trackCount
        )
    }

    override suspend fun getPlaylistTracks(playlistId: Int): List<Track> {
        val trackEntities = appDatabase.playlistTrackDao()
            .getTracksByPlaylistId(playlistId)
            .first()

        return trackEntities.map { entity ->
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
            )
        }
    }

    override suspend fun getTrackCount(playlistId: Int): Int {
        return appDatabase.playlistTrackDao().getTrackCount(playlistId)
    }
}