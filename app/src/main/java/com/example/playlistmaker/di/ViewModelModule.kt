package com.example.playlistmaker.di

import com.example.playlistmaker.mediaLibrary.ui.viewmodel.FavoritesViewModel
import com.example.playlistmaker.mediaLibrary.ui.viewmodel.MediaLibraryViewModel
import com.example.playlistmaker.mediaLibrary.ui.viewmodel.NewPlaylistViewModel
import com.example.playlistmaker.mediaLibrary.ui.viewmodel.PlaylistViewModel
import com.example.playlistmaker.mediaLibrary.ui.viewmodel.PlaylistsViewModel
import com.example.playlistmaker.player.ui.viewmodel.PlayerViewModel
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModel
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SearchViewModel(get()) }
    single { SettingsViewModel(get(), get()) }
    viewModel { NewPlaylistViewModel(get()) }
    viewModel { PlaylistViewModel(get()) }
    viewModel { PlaylistsViewModel(get()) }
    viewModel { FavoritesViewModel(favoriteInteractor = get()) }
    viewModel { MediaLibraryViewModel(get()) }
    viewModel {
        PlayerViewModel(
            favoriteInteractor = get(),
            playlistInteractor = get()
        )
    }
}