package com.example.playlistmaker.search.data.network

import com.example.playlistmaker.search.data.dto.Response
import com.example.playlistmaker.search.data.dto.TrackSearchRequest

class RetrofitNetworkClient(
    private val itunesService: ItunesApi
) : NetworkClient {

    override fun doRequest(dto: Any): Response {
        if (dto is TrackSearchRequest) {
            val resp = itunesService.searchTracks(dto.expression).execute()

            val body = resp.body() ?: Response()

            return body.apply { resultCode = resp.code() }
        } else {
            return Response().apply { resultCode = 400 }
        }
    }

}