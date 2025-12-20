package com.example.playlistmaker.settings.domain.interactor

interface SettingsInteractor {
    fun setDarkThemeEnabled(enabled: Boolean)
    fun isDarkThemeEnabled(): Boolean
    fun applySavedTheme()
}