package com.example.playlistmaker.data.impl

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.data.NetworkClient
import com.example.playlistmaker.data.dto.SearchHistoryDto
import com.example.playlistmaker.data.dto.TrackSearchResponse
import com.example.playlistmaker.data.dto.TrackSearchRequest
import com.example.playlistmaker.data.mapper.SearchHistoryMapper
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.domain.api.repository.TrackRepository
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TrackRepositoryImpl(
    private val networkClient: NetworkClient,
    private val sharedPreferences: SharedPreferences
) : TrackRepository {

    private val gson = Gson()

    override fun searchTracks(expression: String): List<Track> {
        val response = networkClient.doRequest(TrackSearchRequest(expression))
        return if (response.resultCode == 200) {
            (response as TrackSearchResponse).results.map { TrackMapper.mapToDomain(it) }
        } else {
            emptyList()
        }
    }

    override fun getSearchHistory(): List<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<SearchHistoryDto>>() {}.type
            val historyItems = gson.fromJson<List<SearchHistoryDto>>(json, type) ?: emptyList()
            SearchHistoryMapper.historyDtoToTracks(historyItems)
        } else {
            emptyList()
        }
    }

    override fun addToSearchHistory(track: Track) {
        val historyItem = SearchHistoryMapper.trackToHistoryDto(track)
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

    private fun getSearchHistoryItems(): List<SearchHistoryDto> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<SearchHistoryDto>>() {}.type
            gson.fromJson<List<SearchHistoryDto>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun saveSearchHistoryItems(historyItems: List<SearchHistoryDto>) {
        sharedPreferences.edit {
            putString(SEARCH_HISTORY_KEY, gson.toJson(historyItems))
        }
    }

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val MAX_SEARCH_HISTORY_SIZE = 10
    }
}