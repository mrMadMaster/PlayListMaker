package com.example.playlistmaker.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.player.domain.models.PlaybackProgress
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.domain.usecase.PrepareTrackUseCase
import com.example.playlistmaker.player.domain.usecase.ReleasePlayerUseCase
import com.example.playlistmaker.player.domain.usecase.SubscribeToPlaybackProgressUseCase
import com.example.playlistmaker.player.domain.usecase.SubscribeToPlayerStateUseCase
import com.example.playlistmaker.player.domain.usecase.TogglePlaybackUseCase
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    subscribeToStateUseCase: SubscribeToPlayerStateUseCase,
    subscribeToProgressUseCase: SubscribeToPlaybackProgressUseCase,
    private val prepareTrackUseCase: PrepareTrackUseCase,
    private val togglePlaybackUseCase: TogglePlaybackUseCase,
    private val releasePlayerUseCase: ReleasePlayerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        subscribeToStateUseCase()
            .onEach { playerState ->
                _uiState.update { it.copy(playerState = playerState) }
            }
            .launchIn(viewModelScope)

        subscribeToProgressUseCase()
            .onEach { progress ->
                _uiState.update {
                    it.copy(
                        currentTime = progress.formattedCurrent
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun setupTrack(track: Track) {
        val durationMillis = try {
            track.trackTimeMillis.toIntOrNull() ?: 0
        } catch (e: NumberFormatException) {
            0
        }

        _uiState.update {
            it.copy(
                track = track,
                totalTime = PlaybackProgress.formatTime(durationMillis)
            )
        }

        val previewUrl = track.previewUrl
        if (previewUrl.isNullOrEmpty()) {
            _uiState.update {
                it.copy(playerState = PlayerState.Error("error"))
            }
            return
        }

        viewModelScope.launch {
            prepareTrackUseCase.invoke(previewUrl)
        }
    }

    fun togglePlayback() {
        togglePlaybackUseCase.invoke()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            releasePlayerUseCase.invoke()
        }
    }
}