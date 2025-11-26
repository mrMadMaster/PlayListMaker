package com.example.playlistmaker.presentation.creator

import android.content.Context
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.data.impl.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.impl.SettingsRepositoryImpl
import com.example.playlistmaker.data.impl.TrackRepositoryImpl
import com.example.playlistmaker.domain.api.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.api.interactor.SettingsInteractor
import com.example.playlistmaker.domain.api.interactor.TrackInteractor
import com.example.playlistmaker.domain.api.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.api.repository.SettingsRepository
import com.example.playlistmaker.domain.api.repository.TrackRepository
import com.example.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.impl.SettingsInteractorImpl
import com.example.playlistmaker.domain.impl.TrackInteractorImpl

object Creator {

    private fun getTrackRepository(): TrackRepository {
        return TrackRepositoryImpl(RetrofitNetworkClient())
    }

    private fun getSearchHistoryRepository(context: Context): SearchHistoryRepository {
        val sharedPreferences = context.getSharedPreferences("track_history", Context.MODE_PRIVATE)
        return SearchHistoryRepositoryImpl(sharedPreferences)
    }

    private fun getSettingsRepository(context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }

    fun provideTrackInteractor(): TrackInteractor {
        return TrackInteractorImpl(getTrackRepository())
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(getSearchHistoryRepository(context))
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        return SettingsInteractorImpl(getSettingsRepository(context))
    }
}