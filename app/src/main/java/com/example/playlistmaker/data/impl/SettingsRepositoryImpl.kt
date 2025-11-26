package com.example.playlistmaker.data.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.example.playlistmaker.domain.api.repository.SettingsRepository

class SettingsRepositoryImpl (
    context: Context
) : SettingsRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)


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
        private const val APP_PREFERENCES = "app_preferences"
        private const val SWITCH_KEY = "dark_theme_enabled"
    }
}