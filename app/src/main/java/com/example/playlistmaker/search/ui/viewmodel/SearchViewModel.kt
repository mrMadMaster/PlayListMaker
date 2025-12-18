package com.example.playlistmaker.search.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.interactor.TrackInteractor
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
                withContext(Dispatchers.IO) {
                    trackInteractor.searchTracks(currentQuery, object : TrackInteractor.TrackSearchConsumer {
                        override fun onTracksFound(foundTracks: List<Track>) {
                            viewModelScope.launch {
                                _isSearching.value = false
                                if (foundTracks.isEmpty()) {
                                    _searchState.value = SearchState.EmptyResult
                                } else {
                                    _searchState.value = SearchState.Content(foundTracks)
                                }
                            }
                        }

                        override fun onSearchError(exception: Exception) {
                            viewModelScope.launch {
                                _isSearching.value = false
                                _searchState.value = SearchState.Error.NoConnection
                            }
                        }
                    })
                }
            } catch (e: Exception) {
                _isSearching.value = false
                _searchState.value = SearchState.Error.NetworkError(e.message ?: "Unknown error")
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

    sealed class SearchState {
        object Empty : SearchState()
        object Loading : SearchState()
        object EmptyResult : SearchState()
        data class Content(val tracks: List<Track>) : SearchState()

        sealed class Error : SearchState() {
            object NoConnection : Error()
            data class NetworkError(val message: String) : Error()
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}