package com.example.playlistmaker.mediaLibrary.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.mediaLibrary.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.mediaLibrary.domain.models.Playlist
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.utils.TrackInfoFormatter
import kotlinx.coroutines.launch
import java.util.Locale

class PlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _playlist = MutableLiveData<Playlist?>()
    val playlist: LiveData<Playlist?> = _playlist

    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    private val _totalDuration = MutableLiveData<Long>()
    val totalDuration: LiveData<Long> = _totalDuration

    private val _trackCount = MutableLiveData<Int>()
    val trackCount: LiveData<Int> = _trackCount

    private val _deleteTrackResult = MutableLiveData<Boolean?>()
    val deleteTrackResult: LiveData<Boolean?> = _deleteTrackResult

    private val _shareText = MutableLiveData<String>()
    val shareText: LiveData<String> = _shareText

    private val _deletePlaylistResult = MutableLiveData<Boolean?>()
    val deletePlaylistResult: LiveData<Boolean?> = _deletePlaylistResult

    fun loadPlaylistInfo(playlistId: Int) {
        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(playlistId)
            _playlist.value = playlist
            _trackCount.value = playlistInteractor.getPlaylistTracks(playlistId).size
        }
    }

    fun loadPlaylistTracks(playlistId: Int) {
        viewModelScope.launch {
            val tracks = playlistInteractor.getPlaylistTracks(playlistId)
            _tracks.value = tracks
            _totalDuration.value = playlistInteractor.getTotalDuration(playlistId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Int, trackId: Int) {
        viewModelScope.launch {
            val success = playlistInteractor.removeTrackFromPlaylist(playlistId, trackId)
            _deleteTrackResult.value = success
            if (success) {
                loadPlaylistTracks(playlistId)
                loadPlaylistInfo(playlistId)
            }
        }
    }

    fun deletePlaylist(playlistId: Int) {
        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(playlistId)
            playlist?.let {
                val success = playlistInteractor.deletePlaylist(it)
                _deletePlaylistResult.value = success
            }
        }
    }

    fun prepareShareText() {
        viewModelScope.launch {
            val playlist = _playlist.value ?: return@launch
            val tracks = _tracks.value ?: emptyList()

            val shareTextBuilder = StringBuilder()

            shareTextBuilder.appendLine(playlist.name)
            playlist.description?.let {
                if (it.isNotEmpty()) {
                    shareTextBuilder.appendLine(it)
                }
            }
            shareTextBuilder.appendLine(TrackInfoFormatter.getTrackCountText(tracks.size))
            shareTextBuilder.appendLine()

            tracks.forEachIndexed { index, track ->
                shareTextBuilder.appendLine("${index + 1}. ${track.artistName} - ${track.trackName} (${formatTrackDuration(track.trackTimeMillis)})")
            }

            _shareText.value = shareTextBuilder.toString()
        }
    }

    private fun formatTrackDuration(timeMillis: String): String {
        val millis = timeMillis.toLongOrNull() ?: 0L
        val minutes = millis / 1000 / 60
        val seconds = (millis / 1000) % 60
        return String.format(Locale.getDefault(),"%d:%02d", minutes, seconds)
    }
}