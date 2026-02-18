package com.example.playlistmaker.search.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.interactor.TrackInteractor
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.domain.models.SearchState
import com.example.playlistmaker.utils.debounce
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private var currentQuery: String = ""
    private var searchJob: Job? = null

    private val debouncedSearch = debounce<String>(
        delayMillis = SEARCH_DEBOUNCE_DELAY,
        coroutineScope = viewModelScope,
        useLastParam = true
    ) { query ->
        search(query)
    }

    private val debouncedClick = debounce<Track>(
        delayMillis = CLICK_DEBOUNCE_DELAY,
        coroutineScope = viewModelScope,
        useLastParam = true
    ) { track ->
        addToSearchHistory(track)
    }

    private fun search(query: String) {
        searchJob?.cancel()

        _isSearching.value = true
        _searchState.value = SearchState.Loading

        searchJob = trackInteractor.searchTracks(query)
            .onEach { tracks ->
                _isSearching.value = false
                if (tracks.isEmpty()) {
                    _searchState.value = SearchState.EmptyResult
                } else {
                    _searchState.value = SearchState.Content(tracks)
                }
            }
            .catch { e ->
                _isSearching.value = false
                val errorState = SearchState.Error.NoConnection
                _searchState.value = errorState
            }
            .launchIn(viewModelScope)
    }

    fun searchDebounced(query: String) {
        currentQuery = query.trim()

        if (currentQuery.isEmpty()) {
            _searchState.value = SearchState.Empty
            return
        }

        debouncedSearch(currentQuery)
    }

    fun clickDebounced(track: Track) {
        debouncedClick(track)
    }

    fun loadSearchHistory() {
        trackInteractor.getSearchHistory()
            .onEach { history ->
                _searchHistory.value = history
            }
            .catch { e ->
                e.printStackTrace()
            }
            .launchIn(viewModelScope)
    }

    private fun addToSearchHistory(track: Track) {
        viewModelScope.launch {
            try {
                trackInteractor.addToSearchHistory(track)
                loadSearchHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                trackInteractor.clearSearchHistory()
                _searchHistory.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelSearch() {
        searchJob?.cancel()
        _isSearching.value = false
        _searchState.value = SearchState.Empty
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 500L
    }
}