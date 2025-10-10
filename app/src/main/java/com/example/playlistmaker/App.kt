package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

class App : Application() {

    private lateinit var sharedPreferences: SharedPreferences
    var darkTheme = false

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        darkTheme = sharedPreferences.getBoolean(SWITCH_KEY, false)
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        sharedPreferences.edit {
            putBoolean(SWITCH_KEY, darkThemeEnabled)
        }

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
        private const val SWITCH_KEY = "key_for_switch"
    }
}