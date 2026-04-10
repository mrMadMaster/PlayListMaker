package com.example.playlistmaker.mediaLibrary.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.mediaLibrary.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.mediaLibrary.domain.models.Playlist
import kotlinx.coroutines.launch

class NewPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _createPlaylistResult = MutableLiveData<Boolean?>()
    val createPlaylistResult: LiveData<Boolean?> = _createPlaylistResult

    private val _editPlaylistResult = MutableLiveData<Boolean?>()
    val editPlaylistResult: LiveData<Boolean?> = _editPlaylistResult

    private val _playlistForEdit = MutableLiveData<Playlist?>()
    val playlistForEdit: LiveData<Playlist?> = _playlistForEdit

    private val _isEditMode = MutableLiveData(false)

    fun loadPlaylistForEdit(playlistId: Int) {
        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(playlistId)
            _playlistForEdit.value = playlist
            _isEditMode.value = playlist != null
        }
    }

    fun createPlaylist(name: String, description: String?, coverPath: String?) {
        viewModelScope.launch {
            val playlist = Playlist(
                name = name,
                description = description,
                coverPath = coverPath
            )
            playlistInteractor.createPlaylist(playlist, coverPath)
            _createPlaylistResult.postValue(true)
        }
    }

    fun updatePlaylist(id: Int, name: String, description: String?, coverPath: String?) {
        viewModelScope.launch {
            val playlist = Playlist(
                id = id,
                name = name,
                description = description,
                coverPath = coverPath
            )
            val success = playlistInteractor.updatePlaylist(playlist, coverPath)
            _editPlaylistResult.postValue(success)
        }
    }

    fun clearResults() {
        _createPlaylistResult.postValue(null)
        _editPlaylistResult.postValue(null)
    }
}