package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import androidx.core.content.edit

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    fun readHistory(): MutableList<Track> {
        val json = sharedPreferences.getString(TRACK_HISTORY_KEY, null)
        return Gson().fromJson(json, Array<Track>::class.java)?.toMutableList()
            ?: emptyArray<Track>().toMutableList()
    }

    fun writeHistory(track: Track){
        val trackHistory = readHistory()
        if (trackHistory.size == MAX_SIZE) {
            trackHistory.removeAt(trackHistory.lastIndex)
        }
        if (trackHistory.any { it.trackId == track.trackId }) {
            trackHistory.removeIf { it.trackId == track.trackId }
        }
        trackHistory.add(0, track)
        sharedPreferences.edit {
            putString(TRACK_HISTORY_KEY, Gson().toJson(trackHistory))
        }
    }

    fun clearHistory(){
        sharedPreferences.edit {
            remove(TRACK_HISTORY_KEY)
        }
    }

    companion object {
        private const val TRACK_HISTORY_KEY = "track_history"
        private const val MAX_SIZE = 10
    }
}