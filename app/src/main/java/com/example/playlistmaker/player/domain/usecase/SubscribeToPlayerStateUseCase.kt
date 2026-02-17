package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

class SubscribeToPlayerStateUseCase(
    private val repository: PlayerRepository
) {
    operator fun invoke(): Flow<PlayerState> = repository.state
}