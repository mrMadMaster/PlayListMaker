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

class TrackRepositoryImpl(
    private val networkClient: NetworkClient,
    private val sharedPreferences: SharedPreferences
) : TrackRepository {

    private val gson = Gson()

    override fun searchTracks(searchQuery: String): List<Track> {
        val searchRequest = TrackSearchRequest(searchQuery)
        val searchResponse = networkClient.doRequest(searchRequest)

        return if (searchResponse.resultCode == 200) {
            (searchResponse as TrackSearchResponse).results.map { trackDto ->
                TrackMapper.map(trackDto)
            }
        } else {
            emptyList()
        }
    }

    override fun getSearchHistory(): List<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<SearchHistoryItem>>() {}.type
                val historyItems = gson.fromJson<List<SearchHistoryItem>>(json, type) ?: emptyList()
                SearchHistoryMapper.historyItemsToTracks(historyItems)
            } catch (e: Exception) {
                clearSearchHistory()
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override fun addToSearchHistory(track: Track) {
        val historyItem = SearchHistoryMapper.trackToHistoryItem(track)
        val currentHistory = getSearchHistoryItems().toMutableList()

        currentHistory.removeAll { it.trackId == historyItem.trackId }

        currentHistory.add(0, historyItem.copy(addedAt = System.currentTimeMillis()))

        if (currentHistory.size > MAX_SEARCH_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }

        saveSearchHistoryItems(currentHistory)
    }

    override fun clearSearchHistory() {
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
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val MAX_SEARCH_HISTORY_SIZE = 10
    }
}