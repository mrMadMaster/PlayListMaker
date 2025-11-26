package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.domain.api.interactor.SettingsInteractor
import com.example.playlistmaker.presentation.creator.Creator

class App : Application() {

    private lateinit var settingsInteractor: SettingsInteractor

    override fun onCreate() {
        super.onCreate()
        settingsInteractor = Creator.provideSettingsInteractor(this)
        settingsInteractor.applySavedTheme()
    }
}