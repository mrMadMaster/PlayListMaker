package com.example.playlistmaker.search.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.interactor.TrackInteractor
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.models.SearchState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchViewModel(
    private val trackInteractor: TrackInteractor
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private val _searchHistory = MutableLiveData<List<Track>>()
    val searchHistory: LiveData<List<Track>> = _searchHistory

    private val _isSearching = MutableLiveData(false)
    val isSearching: LiveData<Boolean> = _isSearching

    private var searchJob: Job? = null
    private var debounceJob: Job? = null
    private var currentQuery: String = ""


    fun search(query: String) {
        currentQuery = query.trim()

        if (currentQuery.isEmpty()) {
            _searchState.value = SearchState.Empty
            return
        }

        cancelSearch()
        _isSearching.value = true
        _searchState.value = SearchState.Loading

        searchJob = viewModelScope.launch {
            try {

                val tracks = trackInteractor.searchTracks(query)

                _isSearching.value = false
                if (tracks.isEmpty()) {
                    _searchState.value = SearchState.EmptyResult
                } else {
                    _searchState.value = SearchState.Content(tracks)
                }

            }  catch (e: Exception) {
                _isSearching.value = false
                _searchState.value = when {
                    e is java.net.UnknownHostException ||
                            e is java.net.SocketTimeoutException ->
                        SearchState.Error.NoConnection
                    else ->
                        SearchState.Error.NetworkError(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun searchDebounced(query: String) {
        debounceJob?.cancel()

        debounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            search(query)
        }
    }

    fun loadSearchHistory() {
        viewModelScope.launch {
            _searchHistory.value = trackInteractor.getSearchHistory()
        }
    }

    fun addToSearchHistory(track: Track) {
        viewModelScope.launch {
            trackInteractor.addToSearchHistory(track)
            loadSearchHistory()
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            trackInteractor.clearSearchHistory()
            _searchHistory.value = emptyList()
        }
    }

    fun cancelSearch() {
        searchJob?.cancel()
        debounceJob?.cancel()
        _isSearching.value = false
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}