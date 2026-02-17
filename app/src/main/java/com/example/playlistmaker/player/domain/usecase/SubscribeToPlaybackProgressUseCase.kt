package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.models.PlaybackProgress
import com.example.playlistmaker.player.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

class SubscribeToPlaybackProgressUseCase(
    private val repository: PlayerRepository
) {
    operator fun invoke(): Flow<PlaybackProgress> = repository.playbackProgress
}