package com.example.playlistmaker.search.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.interactor.TrackInteractor
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.utils.debounce
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SearchViewModel(
    private val trackInteractor: TrackInteractor
) : ViewModel() {

    private val _state = MutableLiveData<SearchScreenState>()
    val state: LiveData<SearchScreenState> = _state

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

    init {
        loadSearchHistory()
        restoreSearchText()
    }

    fun updateSearchText(text: String) {
        _state.value = _state.value?.copy(searchText = text) ?: SearchScreenState(searchText = text)
    }

    fun restoreSearchText() {
        val text = _state.value?.searchText ?: ""
        if (text.isNotEmpty()) {
            searchDebounced(text)
        }
    }

    fun searchDebounced(query: String) {
        currentQuery = query.trim()
        if (currentQuery.isEmpty()) {
            _state.value = SearchScreenState(uiState = SearchUiState.Empty, searchText = query)
            loadSearchHistory()
            return
        }
        debouncedSearch(currentQuery)
    }

    private fun search(query: String) {
        searchJob?.cancel()
        _state.value = _state.value?.copy(uiState = SearchUiState.Loading, isSearching = true)
            ?: SearchScreenState(uiState = SearchUiState.Loading, isSearching = true)

        searchJob = trackInteractor.searchTracks(query)
            .onEach { tracks ->
                val newState = if (tracks.isEmpty()) {
                    SearchUiState.EmptyResult
                } else {
                    SearchUiState.Content(tracks)
                }
                _state.value = _state.value?.copy(uiState = newState, isSearching = false)
                    ?: SearchScreenState(uiState = newState, isSearching = false)
            }
            .catch { e ->
                _state.value = _state.value?.copy(uiState = SearchUiState.NoConnection, isSearching = false)
                    ?: SearchScreenState(uiState = SearchUiState.NoConnection, isSearching = false)
            }
            .launchIn(viewModelScope)
    }

    fun clickDebounced(track: Track) {
        debouncedClick(track)
    }

    fun loadSearchHistory() {
        trackInteractor.getSearchHistory()
            .onEach { history ->
                val currentState = _state.value ?: SearchScreenState()
                val uiState = if (currentState.searchText.isEmpty() && history.isNotEmpty()) {
                    SearchUiState.History(history)
                } else {
                    currentState.uiState
                }
                _state.value = currentState.copy(uiState = uiState)
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
                _state.value = _state.value?.copy(uiState = SearchUiState.Empty)
                    ?: SearchScreenState(uiState = SearchUiState.Empty)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelSearch() {
        searchJob?.cancel()
        _state.value = _state.value?.copy(uiState = SearchUiState.Empty, isSearching = false)
            ?: SearchScreenState(uiState = SearchUiState.Empty, isSearching = false)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 500L
    }
}