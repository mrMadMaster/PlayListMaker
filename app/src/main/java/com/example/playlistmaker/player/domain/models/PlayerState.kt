package com.example.playlistmaker.player.domain.models

sealed class PlayerState {
    object Idle : PlayerState()
    object Preparing : PlayerState()
    data class Ready(
        val duration: Int,
        val canPlay: Boolean = true
    ) : PlayerState()
    object Playing : PlayerState()
    object Paused : PlayerState()
    object Completed : PlayerState()
}