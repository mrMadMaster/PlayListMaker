package com.example.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.search.data.network.ItunesApi
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.search.data.network.RetrofitNetworkClient
import com.example.playlistmaker.search.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.search.data.repository.TrackRepositoryImpl.Companion.SEARCH_HISTORY_KEY
import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.settings.data.repository.SettingsRepositoryImpl.Companion.APP_PREFERENCES
import com.example.playlistmaker.settings.domain.repository.SettingsRepository
import com.example.playlistmaker.sharing.data.impl.ExternalNavigatorImpl
import com.example.playlistmaker.sharing.data.provider.ResourceSharingConfigProvider
import com.example.playlistmaker.sharing.data.provider.SharingConfigProvider
import com.example.playlistmaker.sharing.domain.model.EmailData
import com.example.playlistmaker.sharing.domain.navigator.ExternalNavigator
import com.google.gson.Gson
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val ITUNES_URL = "https://itunes.apple.com"

val dataModule = module {

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    single<ItunesApi> {
        Retrofit.Builder()
            .baseUrl(ITUNES_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
            .create(ItunesApi::class.java)
    }

    single<NetworkClient> {
        RetrofitNetworkClient(get())
    }

    factory { Gson() }

    single<SharedPreferences>(named("search_prefs")) {
        androidContext().getSharedPreferences(SEARCH_HISTORY_KEY, Context.MODE_PRIVATE)
    }

    single<SharedPreferences>(named("settings_prefs")) {
        androidContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
    }

    single<TrackRepository> {
        TrackRepositoryImpl(
            get(),
            get(named("search_prefs")),
            get()
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            get(named("settings_prefs")))
    }

    single<ExternalNavigator> {
        ExternalNavigatorImpl(
            get()
        )
    }

    single<SharingConfigProvider> {
        ResourceSharingConfigProvider(
            get()
        )
    }

    single<EmailData> {
        get<SharingConfigProvider>().getSharingConfig()
    }

}