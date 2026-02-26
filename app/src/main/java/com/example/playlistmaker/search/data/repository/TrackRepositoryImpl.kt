package com.example.playlistmaker.search.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.search.data.dto.TrackSearchRequest
import com.example.playlistmaker.search.data.dto.TrackSearchResponse
import com.example.playlistmaker.search.data.dto.SearchHistoryItem
import com.example.playlistmaker.search.data.mapper.SearchHistoryMapper
import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class TrackRepositoryImpl(
    private val networkClient: NetworkClient,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : TrackRepository {

    override fun searchTracks(searchQuery: String): Flow<List<Track>> = flow {
        val searchRequest = TrackSearchRequest(searchQuery)
        val searchResponse = networkClient.doRequest(searchRequest)

        when (searchResponse.resultCode) {
            200 -> {
                val tracks = (searchResponse as? TrackSearchResponse)?.results?.map { trackDto ->
                    TrackMapper.map(trackDto)
                } ?: emptyList()
                emit(tracks)
            }
            503, 504 -> {
                throw when (searchResponse.resultCode) {
                    503 -> UnknownHostException("No internet connection")
                    504 -> SocketTimeoutException("Request timeout")
                    else -> Exception("Network error")
                }
            }
            else -> {
                emit(emptyList())
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getSearchHistory(): Flow<List<Track>> = flow {
        val history = getSearchHistoryItems().let { historyItems ->
            SearchHistoryMapper.historyItemsToTracks(historyItems)
        }
        emit(history)
    }.flowOn(Dispatchers.IO)

    override suspend fun addToSearchHistory(track: Track) = withContext(Dispatchers.IO) {
        val historyItem = SearchHistoryMapper.trackToHistoryItem(track)
        val currentHistory = getSearchHistoryItems().toMutableList()

        currentHistory.removeAll { it.trackId == historyItem.trackId }
        currentHistory.add(0, historyItem.copy(addedAt = System.currentTimeMillis()))

        if (currentHistory.size > MAX_SEARCH_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }

        saveSearchHistoryItems(currentHistory)
    }

    override suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            remove(SEARCH_HISTORY_KEY)
        }
    }

    private fun getSearchHistoryItems(): List<SearchHistoryItem> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<SearchHistoryItem>>() {}.type
            gson.fromJson<List<SearchHistoryItem>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun saveSearchHistoryItems(historyItems: List<SearchHistoryItem>) {
        sharedPreferences.edit {
            putString(SEARCH_HISTORY_KEY, gson.toJson(historyItems))
        }
    }

    companion object {
        const val SEARCH_HISTORY_KEY = "search_history"
        private const val MAX_SEARCH_HISTORY_SIZE = 10
    }
}