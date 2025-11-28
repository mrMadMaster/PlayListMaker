package com.example.playlistmaker.data.mapper

import com.example.playlistmaker.data.dto.SearchHistoryDto
import com.example.playlistmaker.domain.models.Track

object SearchHistoryMapper {

    fun trackToHistoryDto(track: Track): SearchHistoryDto {
        return SearchHistoryDto(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTimeMillis = track.trackTimeMillis,
            artworkUrl100 = track.artworkUrl100,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl
        )
    }

    fun historyDtoToTrack(historyItem: SearchHistoryDto): Track {
        return Track(
            trackId = historyItem.trackId,
            trackName = historyItem.trackName,
            artistName = historyItem.artistName,
            trackTimeMillis = historyItem.trackTimeMillis,
            artworkUrl100 = historyItem.artworkUrl100,
            collectionName = historyItem.collectionName,
            releaseDate = historyItem.releaseDate,
            primaryGenreName = historyItem.primaryGenreName,
            country = historyItem.country,
            previewUrl = historyItem.previewUrl
        )
    }

    fun historyDtoToTracks(historyItems: List<SearchHistoryDto>): List<Track> {
        return historyItems.map { historyDtoToTrack(it) }
    }
}