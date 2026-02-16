package com.example.playlistmaker.player.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentAudioPlayerBinding
import com.example.playlistmaker.player.domain.models.PlaybackProgress
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.ui.viewmodel.PlayerViewModel
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale
import java.util.concurrent.TimeUnit

class AudioPlayerFragment : Fragment() {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!

    private val track: Track? by lazy {
        arguments?.getParcelable(TRACK_ARG)
    }

    private val viewModel: PlayerViewModel by lazy {
        ViewModelProvider(requireActivity())[PlayerViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentTrack = track
        if (currentTrack != null) {
            displayTrackInfo(currentTrack)
            viewModel.setupTrack(currentTrack)
            setupClickListeners()
            setupObservers()
        } else {
            parentFragmentManager.popBackStack()
        }
    }

    private fun displayTrackInfo(track: Track) {
        with(binding) {
            trackName.text = track.trackName
            artistName.text = track.artistName
            trackTime2.text = formatTime(track.trackTimeMillis.toLong())

            Glide.with(requireContext())
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
            collectionNameGroup.isVisible = !track.collectionName.isNullOrEmpty()
            if (collectionNameGroup.isVisible) {
                collectionName2.text = track.collectionName
            }

            releaseDateGroup.isVisible = !track.releaseDate.isNullOrEmpty()
            if (releaseDateGroup.isVisible) {
                releaseDate2.text = track.releaseDate?.take(4)
            }

            primaryGenreNameGroup.isVisible = !track.primaryGenreName.isNullOrEmpty()
            if (primaryGenreNameGroup.isVisible) {
                primaryGenreName2.text = track.primaryGenreName
            }

            countryGroup.isVisible = !track.country.isNullOrEmpty()
            if (countryGroup.isVisible) {
                country2.text = track.country
            }
        }
    }

    private fun setupClickListeners() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.startStop.setOnClickListener {
            viewModel.togglePlayback()
        }

        binding.toPlaylist.setOnClickListener {
        }

        binding.toFavourites.setOnClickListener {
        }
    }

    private fun setupObservers() {
        viewModel.playerState.observe(viewLifecycleOwner) { state ->
            updatePlayerState(state)
        }

        viewModel.playbackProgress
            .onEach { progress ->
                updatePlaybackProgress(progress)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun updatePlayerState(state: PlayerState) {
        with(binding) {
            startStop.isEnabled = when (state) {
                is PlayerState.Idle, is PlayerState.Preparing -> false
                else -> true
            }

            startStop.setImageResource(
                when (state) {
                    is PlayerState.Playing -> R.drawable.ic_stop_100
                    else -> R.drawable.ic_start_100
                }
            )
        }
    }

    private fun updatePlaybackProgress(progress: PlaybackProgress?) {
        progress?.let {
            binding.remainingTime.text = it.formattedCurrent
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TRACK_ARG = "track"
        fun createArguments(track: Track): Bundle {
            return Bundle().apply {
                putParcelable(TRACK_ARG, track)
            }
        }
    }
}