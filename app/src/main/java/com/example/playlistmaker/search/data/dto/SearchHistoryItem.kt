package com.example.playlistmaker.search.data.dto

data class SearchHistoryItem(
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: String,
    val artworkUrl100: String,
    val previewUrl: String?,
    val trackId: Int,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val addedAt: Long = System.currentTimeMillis()
)