package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.repository.PlayerRepository

class PrepareTrackUseCase(
    private val repository: PlayerRepository
) {
    suspend operator fun invoke(url: String) {
        repository.prepare(url)
    }
}