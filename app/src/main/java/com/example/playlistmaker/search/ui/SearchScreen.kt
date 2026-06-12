package com.example.playlistmaker.search.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.ui.viewmodel.SearchScreenState
import com.example.playlistmaker.search.ui.viewmodel.SearchUiState
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModel
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import com.example.playlistmaker.ui.components.TopBar
import com.example.playlistmaker.ui.components.TrackItem
import com.example.playlistmaker.ui.theme.PlaylistMakerTheme

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    settingsViewModel: SettingsViewModel,
    onTrackClick: (Track) -> Unit
) {
    val darkTheme by settingsViewModel.themeState.observeAsState(initial = false)
    val state by viewModel.state.observeAsState(initial = SearchScreenState())

    val text = state.searchText
    val uiState = state.uiState
    val isSearching = state.isSearching

    var localText by remember { mutableStateOf(text) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(text) {
        if (localText != text) localText = text
    }

    PlaylistMakerTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() }
        ) {

            TopBar(title = stringResource(R.string.search))

            Spacer(modifier = Modifier.height(8.dp))

            BasicTextField(
                value = localText,
                onValueChange = { newText ->
                    localText = newText
                    viewModel.updateSearchText(newText)
                    viewModel.searchDebounced(newText)
                },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                textStyle = TextStyle(fontSize = 16.sp, lineHeight = 19.sp),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search_16),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(Modifier.weight(1f)) {
                            if (text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.search),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                )
                            }
                            innerTextField()
                        }
                        if (text.isNotEmpty()) {
                            Icon(
                                painter = painterResource(R.drawable.ic_clear_16),
                                contentDescription = stringResource(R.string.clear),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        localText = ""
                                        viewModel.updateSearchText("")
                                        viewModel.searchDebounced("")
                                        focusManager.clearFocus()
                                    },
                                tint = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isSearching -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                else -> {
                    when (uiState) {
                        is SearchUiState.Content -> {
                            TrackList(tracks = uiState.tracks, onTrackClick = { track ->
                                viewModel.clickDebounced(track)
                                onTrackClick(track)
                            })
                        }
                        is SearchUiState.EmptyResult -> {
                            ErrorMessage(R.drawable.ic_not_found_120, stringResource(R.string.not_found))
                        }
                        is SearchUiState.NoConnection -> {
                            ErrorMessage(
                                icon = R.drawable.ic_no_connection_120,
                                text = stringResource(R.string.no_connection),
                                onRefresh = { viewModel.searchDebounced(localText) }
                            )
                        }
                        is SearchUiState.History -> {
                            if (localText.isEmpty() && uiState.tracks.isNotEmpty()) {
                                SearchHistorySection(
                                    history = uiState.tracks,
                                    onTrackClick = { track ->
                                        viewModel.clickDebounced(track)
                                        onTrackClick(track)
                                    },
                                    onClearHistory = { viewModel.clearSearchHistory() }
                                )
                            }
                        }
                        SearchUiState.Empty -> {
                        }
                        SearchUiState.Loading -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun TrackList(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tracks, key = { it.trackId }) { track ->
            TrackItem(track = track, onClick = onTrackClick)
        }
    }
}

@Composable
fun SearchHistorySection(
    history: List<Track>,
    onTrackClick: (Track) -> Unit,
    onClearHistory: () -> Unit
) {
    Column {
        Box(modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(
                text = stringResource(R.string.search_history),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 12.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(history, key = { it.trackId }) { track ->
                TrackItem(track = track, onClick = onTrackClick)
            }
        }
        Button(
            onClick = onClearHistory,
            modifier = Modifier
                .wrapContentSize()
                .padding(top = 24.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
        ) {
            Text(
                text = stringResource(R.string.clear_history),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun ErrorMessage(
    icon: Int,
    text: String,
    onRefresh: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(102.dp))
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (onRefresh != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRefresh,
                shape = RoundedCornerShape(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Text(
                    stringResource(R.string.refresh),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}