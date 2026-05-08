package com.example.playlistmaker.player.data

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.player.domain.service.PlayerServiceConnection
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.domain.models.PlaybackProgress
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PlayerService : Service(), PlayerServiceConnection {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow<PlayerState>(PlayerState.Idle)
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _progress = MutableSharedFlow<PlaybackProgress>(replay = 1)
    override val progress: Flow<PlaybackProgress> = _progress.asSharedFlow()

    private var progressJob: Job? = null
    private var isBackground = false
    private var currentArtist: String = ""
    private var currentTrack: String = ""
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): PlayerServiceConnection = this@PlayerService
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer().apply {
            setOnPreparedListener { mp ->
                scope.launch {
                    _state.value = PlayerState.Ready(mp.duration)
                    startProgressUpdates()
                    updateForegroundNotification()
                }
            }
            setOnCompletionListener {
                scope.launch {
                    _state.value = PlayerState.Completed
                    stopProgressUpdates()
                    _progress.emit(PlaybackProgress.create(0, mediaPlayer?.duration ?: 0))
                    updateForegroundNotification()
                }
            }
            setOnErrorListener { _, what, extra ->
                scope.launch { _state.value = PlayerState.Error("Error: $what, $extra") }
                true
            }
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows current track"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        intent?.getStringExtra(EXTRA_ARTIST)?.let { currentArtist = it }
        intent?.getStringExtra(EXTRA_TRACK_NAME)?.let { currentTrack = it }
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
        scope.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun prepare(url: String, artist: String, trackName: String) {
        release()
        currentArtist = artist
        currentTrack = trackName
        _state.value = PlayerState.Preparing
        try {
            mediaPlayer?.apply {
                reset()
                setDataSource(url)
                prepareAsync()
            }
        } catch (e: Exception) {
            _state.value = PlayerState.Error(e.message ?: "Unknown error")
        }
    }

    override fun playPause() {
        android.util.Log.d("PlayerService", "playPause called, current state = ${_state.value}")
        when (val s = _state.value) {
            is PlayerState.Ready -> play()
            is PlayerState.Paused -> play()
            PlayerState.Completed -> play()
            is PlayerState.Playing -> pause()
            else -> { android.util.Log.w("PlayerService", "ignored in state $s") }
        }
    }

    private fun play() {
        android.util.Log.d("PlayerService", "play() called")
        mediaPlayer?.start()
        _state.value = PlayerState.Playing
        startProgressUpdates()
        updateForegroundNotification()
    }

    private fun pause() {
        android.util.Log.d("PlayerService", "pause() called")
        mediaPlayer?.pause()
        _state.value = PlayerState.Paused
        stopProgressUpdates()
        emitCurrentProgress()
        updateForegroundNotification()
    }

    override fun release() {
        android.util.Log.d("PlayerService", "release() called")
        stopProgressUpdates()
        mediaPlayer?.reset()
        _state.value = PlayerState.Idle
        stopForegroundNotification()
    }

    override fun setAppInBackground(background: Boolean) {
        isBackground = background
        updateForegroundNotification()
    }

    private fun updateForegroundNotification() {
        val playing = _state.value is PlayerState.Playing
        if (isBackground && playing) {
            showForegroundNotification()
        } else {
            stopForegroundNotification()
        }
    }

    private fun showForegroundNotification() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("$currentArtist - $currentTrack")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            else 0)
    }

    private fun stopForegroundNotification() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                emitCurrentProgress()
                delay(300)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun emitCurrentProgress() {
        val mp = mediaPlayer ?: return
        val pos = mp.currentPosition
        val dur = mp.duration
        if (dur > 0) {
            _progress.tryEmit(PlaybackProgress.create(pos, dur))
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "playback_channel"
        const val NOTIFICATION_ID = 101
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_TRACK_NAME = "extra_track_name"
    }
}