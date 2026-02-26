package com.example.playlistmaker.mediaLibrary.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.mediaLibrary.domain.interactor.FavoriteInteractor
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

sealed class FavoritesState {
    object Empty : FavoritesState()
    data class Content(val tracks: List<Track>) : FavoritesState()
}

class FavoritesViewModel(
    private val favoriteInteractor: FavoriteInteractor
) : ViewModel() {

    private val _state = MutableLiveData<FavoritesState>()
    val state: LiveData<FavoritesState> = _state

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        favoriteInteractor.getFavorites()
            .onEach { tracks ->
                _state.postValue(
                    if (tracks.isEmpty()) {
                        FavoritesState.Empty
                    } else {
                        FavoritesState.Content(tracks)
                    }
                )
            }
            .launchIn(viewModelScope)
    }
}