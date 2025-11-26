package com.example.playlistmaker.domain.api.interactor

interface SettingsInteractor {
    fun setDarkThemeEnabled(enabled: Boolean)
    fun isDarkThemeEnabled(): Boolean
    fun applySavedTheme()
}