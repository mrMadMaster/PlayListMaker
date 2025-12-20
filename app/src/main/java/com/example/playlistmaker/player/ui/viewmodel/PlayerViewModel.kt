package com.example.playlistmaker.player.ui.viewmodel

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.domain.models.PlaybackProgress
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    private val _playerState = MutableLiveData<PlayerState>(PlayerState.Idle)
    val playerState: LiveData<PlayerState> = _playerState

    private val _playbackProgress = MutableLiveData<PlaybackProgress?>()
    val playbackProgress: MutableLiveData<PlaybackProgress?> = _playbackProgress

    private var mediaPlayer: MediaPlayer? = null
    private var progressUpdateJob: Job? = null
    private var currentTrack: Track? = null

    fun setupTrack(track: Track) {
        currentTrack = track
        resetPlayer()

        val previewUrl = track.previewUrl

        _playerState.value = PlayerState.Preparing


        mediaPlayer = MediaPlayer().apply {
            setDataSource(previewUrl)
            prepareAsync()

            setOnPreparedListener {
                val duration = it.duration
                _playerState.value = PlayerState.Ready(
                    duration = duration,
                    canPlay = true
                )
                startProgressUpdates()
            }

            setOnCompletionListener {
                _playerState.value = PlayerState.Completed
                stopProgressUpdates()
                resetToBeginning()
            }

        }
    }

    fun togglePlayback() {
        when (_playerState.value) {
            is PlayerState.Ready, is PlayerState.Paused -> play()
            is PlayerState.Playing -> pause()
            is PlayerState.Completed -> {
                resetToBeginning()
                play()
            }
            else -> {}
        }
    }

    private fun play() {
        mediaPlayer?.start()
        _playerState.value = PlayerState.Playing
        startProgressUpdates()
    }

    private fun pause() {
        mediaPlayer?.pause()
        _playerState.value = PlayerState.Paused
        stopProgressUpdates()
    }

    private fun resetToBeginning() {
        mediaPlayer?.seekTo(0)
        updateProgress()
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()

        progressUpdateJob = viewModelScope.launch {
            while (isActive) {
                updateProgress()
                delay(PROGRESS_UPDATE_INTERVAL)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    private fun updateProgress() {
        val currentPosition = mediaPlayer?.currentPosition ?: 0
        val duration = mediaPlayer?.duration ?: 0

        if (duration > 0) {
            _playbackProgress.value = PlaybackProgress.create(currentPosition, duration)
        }
    }

    private fun resetPlayer() {
        stopProgressUpdates()
        mediaPlayer?.release()
        mediaPlayer = null
        _playerState.value = PlayerState.Idle
        _playbackProgress.value = null
    }

    override fun onCleared() {
        super.onCleared()
        resetPlayer()
    }

    companion object {
        private const val PROGRESS_UPDATE_INTERVAL = 300L // 300ms
    }
}