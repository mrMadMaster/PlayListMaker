package com.example.playlistmaker.di

import com.example.playlistmaker.player.domain.usecase.PrepareTrackUseCase
import com.example.playlistmaker.player.domain.usecase.ReleasePlayerUseCase
import com.example.playlistmaker.player.domain.usecase.SubscribeToPlaybackProgressUseCase
import com.example.playlistmaker.player.domain.usecase.SubscribeToPlayerStateUseCase
import com.example.playlistmaker.player.domain.usecase.TogglePlaybackUseCase
import com.example.playlistmaker.search.domain.interactor.TrackInteractor
import com.example.playlistmaker.search.domain.interactor.impl.TrackInteractorImpl
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.example.playlistmaker.settings.domain.interactor.impl.SettingsInteractorImpl
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.sharing.domain.interactor.impl.SharingInteractorImpl
import org.koin.dsl.module

val domainModule = module {

    single<TrackInteractor> {
        TrackInteractorImpl(get())
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }

    single<SharingInteractor> {
        SharingInteractorImpl(get(), get())
    }

    factory { PrepareTrackUseCase(get()) }
    factory { TogglePlaybackUseCase(get()) }
    factory { SubscribeToPlayerStateUseCase(get()) }
    factory { SubscribeToPlaybackProgressUseCase(get()) }
    factory { ReleasePlayerUseCase(get()) }

}