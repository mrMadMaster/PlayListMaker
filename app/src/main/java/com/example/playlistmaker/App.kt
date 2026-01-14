package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.di.dataModule
import com.example.playlistmaker.di.domainModule
import com.example.playlistmaker.di.viewModelModule
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(dataModule, domainModule,  viewModelModule)
        }

        applySavedThemeWithKoin()
    }

    private fun applySavedThemeWithKoin() {
        val settingsInteractor = getKoin().get<SettingsInteractor>()
        settingsInteractor.applySavedTheme()
    }
}