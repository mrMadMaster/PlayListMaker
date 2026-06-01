package com.example.playlistmaker.mediaLibrary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.playlistmaker.R
import com.example.playlistmaker.mediaLibrary.ui.viewmodel.FavoritesState
import com.example.playlistmaker.mediaLibrary.ui.viewmodel.FavoritesViewModel
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.ui.components.TrackItem
import com.example.playlistmaker.ui.theme.PlaylistMakerTheme

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    darkTheme: Boolean,
    onTrackClick: (Track) -> Unit
) {
    val state by viewModel.state.observeAsState(FavoritesState.Empty)

    PlaylistMakerTheme(darkTheme = darkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (state) {
                is FavoritesState.Content -> {
                    val tracks = (state as FavoritesState.Content).tracks
                    if (tracks.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn {
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            items(tracks, key = { it.trackId }) { track ->
                                TrackItem(track = track, onClick = onTrackClick)
                            }
                        }
                    }
                }
                FavoritesState.Empty -> {
                    EmptyState()
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(106.dp))
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.ic_not_found_120),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_media),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}