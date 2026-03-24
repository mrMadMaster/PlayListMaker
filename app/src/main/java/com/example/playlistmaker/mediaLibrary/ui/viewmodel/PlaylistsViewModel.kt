package com.example.playlistmaker.mediaLibrary.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.mediaLibrary.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.mediaLibrary.domain.models.Playlist
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PlaylistsViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    fun loadPlaylists() {
        playlistInteractor.getAllPlaylists()
            .onEach { playlists ->
                val playlistsWithCount = playlists.map { playlist ->
                    val trackCount = playlistInteractor.getPlaylistTracks(playlist.id).size
                    playlist.copy(trackCount = trackCount)
                }
                _playlists.postValue(playlistsWithCount)
            }
            .launchIn(viewModelScope)
    }
}