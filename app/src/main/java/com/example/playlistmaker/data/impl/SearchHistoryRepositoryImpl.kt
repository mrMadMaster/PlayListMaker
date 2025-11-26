package com.example.playlistmaker.data.impl

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.domain.api.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson

class SearchHistoryRepositoryImpl(private val sharedPreferences: SharedPreferences) :
    SearchHistoryRepository {

    private val gson = Gson()

    override fun readHistory(): List<Track> {
        val json = sharedPreferences.getString(TRACK_HISTORY_KEY, null)
        return gson.fromJson(json, Array<Track>::class.java)?.toMutableList()
            ?: emptyArray<Track>().toMutableList()
    }

    override fun writeHistory(track: Track){
        val trackHistory = readHistory().toMutableList()
        if (trackHistory.size == MAX_SIZE) {
            trackHistory.removeAt(trackHistory.lastIndex)
        }
        if (trackHistory.any { it.trackId == track.trackId }) {
            trackHistory.removeIf { it.trackId == track.trackId }
        }
        trackHistory.add(0, track)
        sharedPreferences.edit {
            putString(TRACK_HISTORY_KEY, gson.toJson(trackHistory))
        }
    }

    override fun clearHistory(){
        sharedPreferences.edit {
            remove(TRACK_HISTORY_KEY)
        }
    }

    companion object {
        private const val TRACK_HISTORY_KEY = "track_history"
        private const val MAX_SIZE = 10
    }
}