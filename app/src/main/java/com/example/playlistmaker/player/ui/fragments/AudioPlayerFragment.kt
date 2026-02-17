package com.example.playlistmaker.player.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentAudioPlayerBinding
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.ui.viewmodel.PlayerUiState
import com.example.playlistmaker.player.ui.viewmodel.PlayerViewModel
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioPlayerFragment : Fragment() {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerViewModel by viewModel()

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

        val track = arguments?.getParcelable<Track>(TRACK_ARG)
        if (track != null) {
            displayTrackInfo(track)
            setupClickListeners()
            observeViewModel()
            viewModel.setupTrack(track)
        } else {
            findNavController().popBackStack()
        }
    }

    private fun displayTrackInfo(track: Track) {
        with(binding) {
            trackName.text = track.trackName
            artistName.text = track.artistName
            trackTime2.text = viewModel.uiState.value.totalTime

            Glide.with(requireContext())
                .load(track.getCoverArtwork())
                .centerCrop()
                .placeholder(R.drawable.placeholder_312)
                .error(R.drawable.placeholder_312)
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.cover_radius)))
                .into(cover)

            collectionNameGroup.isVisible = !track.collectionName.isNullOrEmpty()
            collectionNameGroup.takeIf { it.isVisible }?.let {
                collectionName2.text = track.collectionName
            }

            releaseDateGroup.isVisible = !track.releaseDate.isNullOrEmpty()
            releaseDateGroup.takeIf { it.isVisible }?.let {
                releaseDate2.text = track.releaseDate?.take(4)
            }

            primaryGenreNameGroup.isVisible = !track.primaryGenreName.isNullOrEmpty()
            primaryGenreNameGroup.takeIf { it.isVisible }?.let {
                primaryGenreName2.text = track.primaryGenreName
            }

            countryGroup.isVisible = !track.country.isNullOrEmpty()
            countryGroup.takeIf { it.isVisible }?.let {
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

    private fun observeViewModel() {
        viewModel.uiState
            .onEach { state ->
                renderUiState(state)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun renderUiState(state: PlayerUiState) {
        with(binding) {
            remainingTime.text = state.currentTime
            trackTime2.text = state.totalTime

            startStop.isEnabled = state.isPlayButtonEnabled
            startStop.setImageResource(
                if (state.isPlayButtonPlaying) R.drawable.ic_stop_100
                else R.drawable.ic_start_100
            )

            remainingTime.visibility = if (state.showProgress) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.uiState.value.playerState is PlayerState.Playing) {
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