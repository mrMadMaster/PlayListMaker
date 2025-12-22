package com.example.playlistmaker.settings.data.repository

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.example.playlistmaker.settings.domain.repository.SettingsRepository

class SettingsRepositoryImpl (
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    override fun setDarkThemeEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(SWITCH_KEY, enabled)
            apply()
        }

        applyTheme(enabled)
    }

    override fun isDarkThemeEnabled(): Boolean =
        sharedPreferences.getBoolean(SWITCH_KEY, false)

    override fun applySavedTheme() {
        val darkThemeEnabled = isDarkThemeEnabled()
        applyTheme(darkThemeEnabled)
    }

    private fun applyTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    companion object {
        const val APP_PREFERENCES = "app_preferences"
        private const val SWITCH_KEY = "dark_theme_enabled"
    }
}