package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.repository.PlayerRepository

class ReleasePlayerUseCase(
    private val repository: PlayerRepository
) {
    operator fun invoke() {
        repository.release()
    }
}