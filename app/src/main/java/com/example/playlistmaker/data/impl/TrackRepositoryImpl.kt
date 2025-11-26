package com.example.playlistmaker.data.impl

import com.example.playlistmaker.data.NetworkClient
import com.example.playlistmaker.data.dto.TrackSearchResponse
import com.example.playlistmaker.data.dto.TrackSearchRequest
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.domain.api.repository.TrackRepository
import com.example.playlistmaker.domain.models.Track

class TrackRepositoryImpl (private val networkClient: NetworkClient):
    TrackRepository {

    override fun searchTracks(expression: String): List<Track> {
        val response = networkClient.doRequest(TrackSearchRequest(expression))
        return if (response.resultCode == 200) {
            (response as TrackSearchResponse).results.map { TrackMapper.mapToDomain(it) }
        } else {
            emptyList()
        }
    }
}