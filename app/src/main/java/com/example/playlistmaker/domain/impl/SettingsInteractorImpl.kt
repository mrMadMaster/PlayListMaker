package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.interactor.SettingsInteractor
import com.example.playlistmaker.domain.api.repository.SettingsRepository

class SettingsInteractorImpl (private val repository: SettingsRepository): SettingsInteractor {
    override fun setDarkThemeEnabled(enabled: Boolean) {
        repository.setDarkThemeEnabled(enabled)
    }

    override fun isDarkThemeEnabled(): Boolean {
        return repository.isDarkThemeEnabled()
    }

    override fun applySavedTheme() {
        repository.applySavedTheme()
    }
}