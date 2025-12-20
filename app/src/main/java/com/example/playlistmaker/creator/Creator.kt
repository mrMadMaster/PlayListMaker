package com.example.playlistmaker.creator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.search.data.network.RetrofitNetworkClient
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.search.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.search.domain.interactor.TrackInteractor
import com.example.playlistmaker.search.domain.interactor.impl.TrackInteractorImpl
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModel
import com.example.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.example.playlistmaker.settings.domain.interactor.impl.SettingsInteractorImpl
import com.example.playlistmaker.settings.domain.repository.SettingsRepository
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import com.example.playlistmaker.sharing.data.impl.ExternalNavigatorImpl
import com.example.playlistmaker.sharing.data.provider.ResourceSharingConfigProvider
import com.example.playlistmaker.sharing.data.provider.SharingConfigProvider
import com.example.playlistmaker.sharing.domain.interactor.impl.SharingInteractorImpl
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.sharing.domain.navigator.ExternalNavigator

object Creator {

    fun provideSearchViewModelFactory(context: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val trackInteractor = provideTrackInteractor(context)
                return SearchViewModel(trackInteractor) as T
            }
        }
    }

    fun provideSettingsViewModelFactory(context: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val settingsInteractor = provideSettingsInteractor(context)
                val sharingInteractor = provideSharingInteractor(context)
                return SettingsViewModel(settingsInteractor, sharingInteractor) as T
            }
        }
    }

    private fun provideTrackInteractor(context: Context): TrackInteractor {
        val trackRepository = provideTrackRepository(context)
        return TrackInteractorImpl(trackRepository)
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        val settingsRepository = provideSettingsRepository(context)
        return SettingsInteractorImpl(settingsRepository)
    }

    fun provideExternalNavigator(context: Context): ExternalNavigator {
        return ExternalNavigatorImpl(context)
    }

    fun provideSharingConfigProvider(context: Context): SharingConfigProvider {
        return ResourceSharingConfigProvider(context)
    }

    fun provideSharingInteractor(context: Context): SharingInteractor {
        val externalNavigator = provideExternalNavigator(context)
        val configProvider = provideSharingConfigProvider(context)
        val config = configProvider.getSharingConfig()

        return SharingInteractorImpl(externalNavigator, config)
    }

    private fun provideTrackRepository(context: Context): com.example.playlistmaker.search.domain.repository.TrackRepository {
        val networkClient = provideNetworkClient()
        val sharedPreferences = provideSharedPreferences(context)
        return TrackRepositoryImpl(
            networkClient,
            sharedPreferences
        )
    }

    private fun provideSettingsRepository(context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }

    private fun provideSharedPreferences(context: Context): android.content.SharedPreferences {
        return context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    private fun provideNetworkClient(): NetworkClient {
        return RetrofitNetworkClient()
    }
}