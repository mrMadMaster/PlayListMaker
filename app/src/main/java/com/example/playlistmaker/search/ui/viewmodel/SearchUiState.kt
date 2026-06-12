package com.example.playlistmaker.search.ui.viewmodel

import com.example.playlistmaker.search.domain.models.Track

sealed class SearchUiState {
    object Empty : SearchUiState()
    object Loading : SearchUiState()
    data class Content(val tracks: List<Track>) : SearchUiState()
    object EmptyResult : SearchUiState()
    object NoConnection : SearchUiState()
    data class History(val tracks: List<Track>) : SearchUiState()
}

data class SearchScreenState(
    val uiState: SearchUiState = SearchUiState.Empty,
    val searchText: String = "",
    val isSearching: Boolean = false
)