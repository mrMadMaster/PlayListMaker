package com.example.playlistmaker.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.mediaLibrary.domain.interactor.FavoriteInteractor
import com.example.playlistmaker.mediaLibrary.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.mediaLibrary.domain.models.Playlist
import com.example.playlistmaker.player.domain.service.PlayerServiceConnection
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val favoriteInteractor: FavoriteInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private var service: PlayerServiceConnection? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _addToPlaylistResult = MutableStateFlow<String?>(null)
    val addToPlaylistResult: StateFlow<String?> = _addToPlaylistResult.asStateFlow()

    private var currentTrack: Track? = null
    private var stateJob: kotlinx.coroutines.Job? = null
    private var progressJob: kotlinx.coroutines.Job? = null

    fun onServiceConnected(service: PlayerServiceConnection) {
        this.service = service
        subscribeToService()
    }

    private fun subscribeToService() {
        val srv = service ?: return
        stateJob?.cancel()
        stateJob = viewModelScope.launch {
            srv.state.collect { playerState ->
                _uiState.update { it.copy(playerState = playerState) }
            }
        }
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            srv.progress.collect { progress ->
                _uiState.update { it.copy(currentTime = progress.formattedCurrent) }
            }
        }
    }

    fun setupTrack(track: Track) {
        currentTrack = track
        viewModelScope.launch {
            val favoriteIds = favoriteInteractor.getFavoriteIds()
            val isTrackFavorite = favoriteIds.contains(track.trackId)
            _uiState.update {
                it.copy(
                    track = track.apply { this.isFavorite = isTrackFavorite },
                    currentTime = "00:00"
                )
            }
            _isFavorite.value = isTrackFavorite
        }
        service?.prepare(track)
    }

    fun togglePlayback() {
        service?.playPause()
    }

    fun onFavoriteClicked() {
        val track = currentTrack ?: return
        viewModelScope.launch {
            if (track.isFavorite) {
                favoriteInteractor.removeFromFavorites(track)
            } else {
                favoriteInteractor.addToFavorites(track)
            }
            track.isFavorite = !track.isFavorite
            _isFavorite.value = track.isFavorite
            _uiState.update {
                it.copy(track = it.track?.apply { this.isFavorite = track.isFavorite })
            }
        }
    }

    fun loadPlaylists() {
        playlistInteractor.getAllPlaylists()
            .onEach { playlists ->
                val playlistsWithCount = playlists.map { playlist ->
                    val trackCount = playlistInteractor.getPlaylistTracks(playlist.id).size
                    playlist.copy(trackCount = trackCount)
                }
                _playlists.value = playlistsWithCount
            }
            .launchIn(viewModelScope)
    }

    fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        viewModelScope.launch {
            val success = playlistInteractor.addTrackToPlaylist(playlist, track)
            if (success) {
                _addToPlaylistResult.value = "Добавлено в плейлист ${playlist.name}"
                loadPlaylists()
            } else {
                _addToPlaylistResult.value = "Трек уже добавлен в плейлист ${playlist.name}"
            }
        }
    }

    fun clearAddToPlaylistResult() {
        _addToPlaylistResult.value = null
    }

    fun onAppBackgroundChanged(background: Boolean) {
        service?.setAppInBackground(background)
    }

    override fun onCleared() {
        super.onCleared()
        stateJob?.cancel()
        progressJob?.cancel()
    }
}