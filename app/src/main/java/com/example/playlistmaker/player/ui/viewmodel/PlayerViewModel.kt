package com.example.playlistmaker.player.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.mediaLibrary.domain.interactor.FavoriteInteractor
import com.example.playlistmaker.mediaLibrary.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.mediaLibrary.domain.models.Playlist
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.domain.usecase.PrepareTrackUseCase
import com.example.playlistmaker.player.domain.usecase.ReleasePlayerUseCase
import com.example.playlistmaker.player.domain.usecase.SubscribeToPlaybackProgressUseCase
import com.example.playlistmaker.player.domain.usecase.SubscribeToPlayerStateUseCase
import com.example.playlistmaker.player.domain.usecase.TogglePlaybackUseCase
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PlayerViewModel(
    subscribeToStateUseCase: SubscribeToPlayerStateUseCase,
    subscribeToProgressUseCase: SubscribeToPlaybackProgressUseCase,
    private val prepareTrackUseCase: PrepareTrackUseCase,
    private val togglePlaybackUseCase: TogglePlaybackUseCase,
    private val releasePlayerUseCase: ReleasePlayerUseCase,
    private val favoriteInteractor: FavoriteInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _uiState = MutableLiveData(PlayerUiState())
    val uiState: LiveData<PlayerUiState> = _uiState

    private val _isFavorite = MutableLiveData(false)
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _addToPlaylistResult = MutableLiveData<String?>()
    val addToPlaylistResult: LiveData<String?> = _addToPlaylistResult

    private var currentTrack: Track? = null

    init {
        subscribeToStateUseCase()
            .onEach { playerState ->
                _uiState.postValue(_uiState.value?.copy(playerState = playerState))
            }
            .launchIn(viewModelScope)

        subscribeToProgressUseCase()
            .onEach { progress ->
                _uiState.postValue(
                    _uiState.value?.copy(
                        currentTime = progress.formattedCurrent
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    fun setupTrack(track: Track) {
        currentTrack = track

        viewModelScope.launch {
            val favoriteIds = favoriteInteractor.getFavoriteIds()
            val isTrackFavorite = favoriteIds.contains(track.trackId)

            _uiState.postValue(
                _uiState.value?.copy(
                    track = track.apply { isFavorite = isTrackFavorite },
                    currentTime = "00:00"
                )
            )

            _isFavorite.postValue(isTrackFavorite)
        }

        val previewUrl = track.previewUrl
        if (previewUrl.isNullOrEmpty()) {
            _uiState.postValue(
                _uiState.value?.copy(playerState = PlayerState.Error("error"))
            )
            return
        }

        viewModelScope.launch {
            prepareTrackUseCase.invoke(previewUrl)
        }
    }

    fun togglePlayback() {
        togglePlaybackUseCase.invoke()
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
            _isFavorite.postValue(track.isFavorite)

            _uiState.postValue(
                _uiState.value?.copy(
                    track = _uiState.value?.track?.apply { isFavorite = track.isFavorite }
                )
            )
        }
    }

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

    fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        viewModelScope.launch {
            val success = playlistInteractor.addTrackToPlaylist(playlist, track)

            if (success) {
                _addToPlaylistResult.postValue(
                    "Добавлено в плейлист ${playlist.name}"
                )
                loadPlaylists()
            } else {
                _addToPlaylistResult.postValue(
                    "Трек уже добавлен в плейлист ${playlist.name}"
                )
            }
        }
    }

    fun clearAddToPlaylistResult() {
        _addToPlaylistResult.postValue(null)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            releasePlayerUseCase.invoke()
        }
    }
}