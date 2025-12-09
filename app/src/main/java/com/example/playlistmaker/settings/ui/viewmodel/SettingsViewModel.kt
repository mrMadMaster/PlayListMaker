package com.example.playlistmaker.settings.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import kotlinx.coroutines.launch




import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor


class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor,
    private val sharingInteractor: SharingInteractor
) : ViewModel() {

    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    private val _uiState = MutableLiveData<SettingsUiState>()

    init {
        loadThemeState()
    }

    fun loadThemeState() {
        viewModelScope.launch {
            val isDarkTheme = settingsInteractor.isDarkThemeEnabled()
            _themeState.postValue(isDarkTheme)
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            settingsInteractor.setDarkThemeEnabled(enabled)
            _themeState.postValue(enabled)
            _uiState.postValue(SettingsUiState.ThemeChanged(enabled))
        }
    }

    fun shareApp() {
        viewModelScope.launch {
            sharingInteractor.shareApp()
        }
    }

    fun openSupport() {
        viewModelScope.launch {
            sharingInteractor.openSupport()
        }
    }

    fun openUserAgreement() {
        viewModelScope.launch {
            sharingInteractor.openUserAgreement()
        }
    }

    sealed class SettingsUiState {
        data class ThemeChanged(val isDarkTheme: Boolean) : SettingsUiState()
    }
}