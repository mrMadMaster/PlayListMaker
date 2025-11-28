package com.example.playlistmaker.presentation.ui.audioplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var trackTime: TextView
    private lateinit var remainingTime: TextView
    private lateinit var artwork: ImageView
    private lateinit var collectionName: TextView
    private lateinit var releaseDate: TextView
    private lateinit var primaryGenreName: TextView
    private lateinit var country: TextView
    private lateinit var collectionNameGroup: Group
    private lateinit var releaseDateGroup: Group
    private lateinit var primaryGenreNameGroup: Group
    private lateinit var countryGroup: Group
    private lateinit var startStopButton: ImageView
    private var playerState = PlayerState.DEFAULT
    private var mediaPlayer = MediaPlayer()
    private val handler = Handler(Looper.getMainLooper())

    private val timeFormatter by lazy {
        SimpleDateFormat("mm:ss", Locale.getDefault())
    }

    private val trackTimerRunnable = object : Runnable {
        override fun run() {
            if (playerState == PlayerState.PLAYING) {
                remainingTime.text = timeFormatter.format(mediaPlayer.currentPosition)
                handler.postDelayed(this, UPDATE_INTERVAL)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        trackName = findViewById(R.id.trackName)
        artistName = findViewById(R.id.artistName)
        trackTime = findViewById(R.id.trackTime2)
        remainingTime = findViewById(R.id.remainingTime)
        artwork = findViewById(R.id.cover)
        collectionName = findViewById(R.id.collectionName2)
        releaseDate = findViewById(R.id.releaseDate2)
        primaryGenreName = findViewById(R.id.primaryGenreName2)
        country = findViewById(R.id.country2)
        collectionNameGroup = findViewById(R.id.collectionNameGroup)
        releaseDateGroup = findViewById(R.id.releaseDateGroup)
        primaryGenreNameGroup = findViewById(R.id.primaryGenreNameGroup)
        countryGroup = findViewById(R.id.countryGroup)
        startStopButton = findViewById(R.id.start_stop)

        startStopButton.isEnabled = false
        startStopButton.setOnClickListener {
            playbackControl()
        }

        val backButton = findViewById<ImageButton>(R.id.back)

        backButton.setOnClickListener {
            finish()
        }

        preparePlayer()
    }

    fun preparePlayer(){
        @Suppress("DEPRECATION")
        val track = intent.getParcelableExtra<Track>("TRACK")

        if (track != null) {
            trackName.text = track.trackName
            artistName.text = track.artistName
            trackTime.text = timeFormatter.format(track.trackTimeMillis.toLong())
            mediaPlayer.setDataSource(track.previewUrl)
            mediaPlayer.prepareAsync()

            mediaPlayer.setOnPreparedListener {
                startStopButton.isEnabled = true
                playerState = PlayerState.PREPARED
                changeStartStopIcon()
            }

            mediaPlayer.setOnCompletionListener {
                playerState = PlayerState.PREPARED
                stopTrackTimer()
                changeStartStopIcon()
                remainingTime.text = getString(R.string.remainingTime)
            }

            if (track.collectionName.isNullOrEmpty()) {
                collectionNameGroup.visibility = View.GONE
            } else {
                collectionName.text = track.collectionName
            }

            if (track.releaseDate.isNullOrEmpty()) {
                releaseDateGroup.visibility = View.GONE
            } else {
                releaseDate.text = track.releaseDate.substring(0, 4)
            }

            if (track.primaryGenreName.isNullOrEmpty()) {
                primaryGenreNameGroup.visibility = View.GONE
            } else {
                primaryGenreName.text = track.primaryGenreName
            }

            if (track.country.isNullOrEmpty()) {
                countryGroup.visibility = View.GONE
            } else {
                country.text = track.country
            }

            Glide.with(this)
                .load(track.getCoverArtwork())
                .centerCrop()
                .placeholder(R.drawable.placeholder_312)
                .error(R.drawable.placeholder_312)
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.cover_radius)))
                .into(artwork)
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playerState = PlayerState.PLAYING
        startTrackTimer()
        changeStartStopIcon()
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playerState = PlayerState.PAUSED
        stopTrackTimer()
        changeStartStopIcon()
    }

    private fun playbackControl() {
        when(playerState) {
            PlayerState.PLAYING -> {
                pausePlayer()
            }
            PlayerState.PREPARED, PlayerState.PAUSED -> {
                startPlayer()
            }
            PlayerState.DEFAULT -> {}
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTrackTimer()
        mediaPlayer.release()
    }

    private fun startTrackTimer() {
        handler.post(trackTimerRunnable)
    }

    private fun stopTrackTimer() {
        handler.removeCallbacks(trackTimerRunnable)
    }

    private fun changeStartStopIcon() {
        startStopButton.setImageResource(
            if (mediaPlayer.isPlaying) R.drawable.ic_stop_100 else R.drawable.ic_start_100
        )
    }

    enum class PlayerState {
        DEFAULT,
        PREPARED,
        PLAYING,
        PAUSED
    }

    companion object {
        private const val UPDATE_INTERVAL = 300L
    }
}