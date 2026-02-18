package com.example.playlistmaker.search.data.network

import com.example.playlistmaker.search.data.dto.Response
import com.example.playlistmaker.search.data.dto.TrackSearchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.UnknownHostException

class RetrofitNetworkClient(
    private val itunesService: ItunesApi
) : NetworkClient {

    override suspend fun doRequest(dto: Any): Response = withContext(Dispatchers.IO) {
        if (dto is TrackSearchRequest) {
            try {
                val response = itunesService.searchTracks(dto.expression)
                response.resultCode = 200
                response
            } catch (e: UnknownHostException) {
                android.util.Log.e("NetworkClient", "UnknownHostException", e)
                Response().apply { resultCode = 503 }
            } catch (e: ConnectException) {
                android.util.Log.e("NetworkClient", "ConnectException", e)
                Response().apply { resultCode = 503 }
            } catch (e: Exception) {
                android.util.Log.e("NetworkClient", "Other exception: ${e::class.simpleName}", e)
                Response().apply { resultCode = 500 }
            }
        } else {
            Response().apply { resultCode = 400 }
        }
    }
}