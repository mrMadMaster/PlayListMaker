package com.example.playlistmaker.search.data.mapper

import com.example.playlistmaker.search.data.dto.SearchHistoryItem
import com.example.playlistmaker.search.domain.models.Track

object SearchHistoryMapper {

    fun trackToHistoryItem(track: Track): SearchHistoryItem {
        return SearchHistoryItem(
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

    fun historyItemToTrack(historyItem: SearchHistoryItem): Track {
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

    fun historyItemsToTracks(historyItems: List<SearchHistoryItem>): List<Track> {
        return historyItems.map { historyItemToTrack(it) }
    }
}