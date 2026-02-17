package com.example.playlistmaker.player.data.repository

import android.media.MediaPlayer
import com.example.playlistmaker.player.domain.models.PlaybackProgress
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.domain.repository.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerRepositoryImpl (
    private val mediaPlayer: MediaPlayer
) : PlayerRepository {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow<PlayerState>(PlayerState.Idle)
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _playbackProgress = MutableSharedFlow<PlaybackProgress>(replay = 1)
    override val playbackProgress: Flow<PlaybackProgress> = _playbackProgress.asSharedFlow()

    private var progressUpdateJob: Job? = null

    init {
        setupListeners()
    }

    private fun setupListeners() {
        mediaPlayer.setOnPreparedListener { mp ->
            scope.launch {
                _state.update { PlayerState.Ready(mp.duration) }
                updateProgress()
            }
        }

        mediaPlayer.setOnCompletionListener {
            scope.launch {
                _state.update { PlayerState.Completed }
                stopProgressUpdates()
                updateProgress(resetToZero = true)
            }
        }

        mediaPlayer.setOnErrorListener { _, what, extra ->
            scope.launch {
                _state.update { PlayerState.Error("Error: $what, $extra") }
            }
            true
        }
    }

    override suspend fun prepare(url: String) {
        release()

        _state.update { PlayerState.Preparing }

        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            _state.update { PlayerState.Error(e.message ?: "Unknown error") }
        }
    }

    override fun play() {
        when (_state.value) {
            is PlayerState.Ready -> {
                mediaPlayer.start()
                _state.update { PlayerState.Playing }
                startProgressUpdates()
            }
            is PlayerState.Paused -> {
                mediaPlayer.start()
                _state.update { PlayerState.Playing }
                startProgressUpdates()
            }
            PlayerState.Completed -> {
                mediaPlayer.seekTo(0)
                mediaPlayer.start()
                _state.update { PlayerState.Playing }
                startProgressUpdates()
            }
            else -> {}
        }
    }

    override fun pause() {
        if (_state.value is PlayerState.Playing) {
            mediaPlayer.pause()
            _state.update { PlayerState.Paused }
            stopProgressUpdates()
            updateProgress()
        }
    }

    override fun togglePlayback() {
        when (_state.value) {
            is PlayerState.Playing -> pause()
            is PlayerState.Ready, is PlayerState.Paused, is PlayerState.Completed -> play()
            else -> {}
        }
    }

    override fun release() {
        stopProgressUpdates()
        mediaPlayer.reset()
        _state.update { PlayerState.Idle }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()

        progressUpdateJob = scope.launch {
            while (true) {
                updateProgress()
                delay(300)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    private fun updateProgress(resetToZero: Boolean = false) {
        val currentPosition = if (resetToZero) {
            0
        } else {
            mediaPlayer.currentPosition
        }

        val duration = mediaPlayer.duration

        if (duration > 0) {
            scope.launch {
                _playbackProgress.emit(
                    PlaybackProgress.create(currentPosition, duration)
                )
            }
        }
    }
}