package com.example.playlistmaker.player.domain.repository

import com.example.playlistmaker.player.domain.models.PlaybackProgress
import com.example.playlistmaker.player.domain.models.PlayerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {
    val state: StateFlow<PlayerState>
    val playbackProgress: Flow<PlaybackProgress>

    suspend fun prepare(url: String)
    fun play()
    fun pause()
    fun togglePlayback()
    fun release()
}