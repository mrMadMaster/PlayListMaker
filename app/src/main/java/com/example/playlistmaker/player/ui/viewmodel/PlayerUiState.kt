package com.example.playlistmaker.player.ui.viewmodel

import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.search.domain.models.Track

data class PlayerUiState(
    val playerState: PlayerState = PlayerState.Idle,
    val track: Track? = null,
    val currentTime: String = "00:00",
    val totalTime: String = "00:00"
) {
    val isPlayButtonEnabled: Boolean
        get() = when (playerState) {
            is PlayerState.Ready,
            is PlayerState.Paused,
            is PlayerState.Playing,
            is PlayerState.Completed -> true
            else -> false
        }

    val isPlayButtonPlaying: Boolean
        get() = playerState is PlayerState.Playing

    val showProgress: Boolean
        get() = playerState !is PlayerState.Idle && playerState !is PlayerState.Preparing
}