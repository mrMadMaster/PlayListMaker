package com.example.playlistmaker.player.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityAudioPlayerBinding
import com.example.playlistmaker.player.domain.models.PlaybackProgress
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.ui.viewmodel.PlayerViewModel
import com.example.playlistmaker.search.domain.models.Track
import java.util.Locale
import java.util.concurrent.TimeUnit

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioPlayerBinding

    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadTrackFromIntent()
        setupClickListeners()
        setupObservers()
    }

    private fun loadTrackFromIntent() {
        val track = intent.getParcelableExtra<Track>("TRACK")
        if (track != null) {
            displayTrackInfo(track)
            viewModel.setupTrack(track)
        } else {
            finish()
        }
    }

    private fun displayTrackInfo(track: Track) {
        with(binding) {
            trackName.text = track.trackName
            artistName.text = track.artistName
            trackTime2.text = formatTime(track.trackTimeMillis.toLong())

            Glide.with(this@AudioPlayerActivity)
                .load(track.getCoverArtwork())
                .centerCrop()
                .placeholder(R.drawable.placeholder_312)
                .error(R.drawable.placeholder_312)
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.cover_radius)))
                .into(cover)

            displayOptionalInfo(track)
        }
    }

    private fun displayOptionalInfo(track: Track) {
        with(binding) {

            if (!track.collectionName.isNullOrEmpty()) {
                collectionName2.text = track.collectionName
                collectionNameGroup.isVisible = true
            } else {
                collectionNameGroup.isVisible = false
            }

            if (!track.releaseDate.isNullOrEmpty()) {
                releaseDate2.text = track.releaseDate.take(4)
                releaseDateGroup.isVisible = true
            } else {
                releaseDateGroup.isVisible = false
            }

            if (!track.primaryGenreName.isNullOrEmpty()) {
                primaryGenreName2.text = track.primaryGenreName
                primaryGenreNameGroup.isVisible = true
            } else {
                primaryGenreNameGroup.isVisible = false
            }

            if (!track.country.isNullOrEmpty()) {
                country2.text = track.country
                countryGroup.isVisible = true
            } else {
                countryGroup.isVisible = false
            }
        }
    }

    private fun setupClickListeners() {
        binding.back.setOnClickListener {
            finish()
        }

        binding.startStop.setOnClickListener {
            viewModel.togglePlayback()
        }
    }

    private fun setupObservers() {
        viewModel.playerState.observe(this) { state ->
            updatePlayerState(state)
        }

        viewModel.playbackProgress.observe(this) { progress ->
            updatePlaybackProgress(progress)
        }
    }

    private fun updatePlayerState(state: PlayerState) {
        with(binding) {
            when (state) {
                is PlayerState.Idle -> {
                    startStop.isEnabled = false
                    startStop.setImageResource(R.drawable.ic_start_100)
                }

                is PlayerState.Preparing -> {
                    startStop.isEnabled = false
                    startStop.setImageResource(R.drawable.ic_start_100)
                }

                is PlayerState.Ready -> {
                    startStop.isEnabled = state.canPlay
                    startStop.setImageResource(R.drawable.ic_start_100)
                }

                is PlayerState.Playing -> {
                    startStop.isEnabled = true
                    startStop.setImageResource(R.drawable.ic_stop_100)
                }

                is PlayerState.Paused -> {
                    startStop.isEnabled = true
                    startStop.setImageResource(R.drawable.ic_start_100)
                }

                is PlayerState.Completed -> {
                    startStop.isEnabled = true
                    startStop.setImageResource(R.drawable.ic_start_100)
                }

            }
        }
    }

    private fun updatePlaybackProgress(progress: PlaybackProgress?) {
        progress?.let {
            with(binding) {
                remainingTime.text = it.formattedCurrent
            }
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.playerState.value is PlayerState.Playing) {
            viewModel.togglePlayback()
        }
    }

}