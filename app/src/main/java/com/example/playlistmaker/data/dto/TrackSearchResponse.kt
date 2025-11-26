package com.example.playlistmaker.data.dto


data class TrackSearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
) : Response()