package com.example.playlistmaker.player.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentAudioPlayerBinding
import com.example.playlistmaker.mediaLibrary.domain.models.Playlist
import com.example.playlistmaker.mediaLibrary.ui.adapter.PlaylistSmallAdapter
import com.example.playlistmaker.player.domain.models.PlaybackProgress
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.ui.viewmodel.PlayerUiState
import com.example.playlistmaker.player.ui.viewmodel.PlayerViewModel
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.utils.CustomSnackbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioPlayerFragment : Fragment() {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerViewModel by viewModel()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var playlistAdapter: PlaylistSmallAdapter

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
            setupBottomSheet()
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
            val durationMillis = try {
                track.trackTimeMillis.toIntOrNull() ?: 0
            } catch (e: NumberFormatException) {
                0
            }
            trackTime2.text = PlaybackProgress.formatTime(durationMillis)

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

        binding.toFavourites.setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        binding.toPlaylist.setOnClickListener {
            showPlaylistBottomSheet()
        }
    }

    private fun setupBottomSheet() {
        val bottomSheet = binding.playlistsBottomSheet.root
        val density = resources.displayMetrics.density
        val peekHeightInPx = (505 * density).toInt()

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
            isDraggable = true
            peekHeight = peekHeightInPx
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (!isAdded || _binding == null) return

                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.visibility = View.GONE
                        binding.playlistsBottomSheet.root.visibility = View.GONE
                    }
                    else -> {
                        binding.overlay.visibility = View.VISIBLE
                        binding.playlistsBottomSheet.root.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        playlistAdapter = PlaylistSmallAdapter { playlist ->
            onPlaylistSelected(playlist)
        }

        binding.playlistsBottomSheet.playlistsRecyclerView.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.playlistsBottomSheet.btnNewPlaylist.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val action = AudioPlayerFragmentDirections.actionAudioPlayerFragmentToNewPlaylistFragment()
            findNavController().navigate(action)
        }

        binding.overlay.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.playlistsBottomSheet.root.visibility = View.GONE
        binding.overlay.visibility = View.GONE
    }

    private fun showPlaylistBottomSheet() {
        viewModel.loadPlaylists()
        binding.playlistsBottomSheet.root.visibility = View.VISIBLE
        binding.overlay.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun onPlaylistSelected(playlist: Playlist) {
        val track = viewModel.uiState.value?.track ?: return
        viewModel.addTrackToPlaylist(playlist, track)
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
                renderUiState(uiState, isFavorite)
            }
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistAdapter.updatePlaylists(playlists)
        }

        viewModel.addToPlaylistResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                CustomSnackbar.show(binding.root, it)
                viewModel.clearAddToPlaylistResult()

                if (it.startsWith("Добавлено")) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    viewModel.loadPlaylists()
                }
            }
        }
    }

    private fun renderUiState(state: PlayerUiState, isFavorite: Boolean) {
        with(binding) {
            remainingTime.text = state.currentTime

            toFavourites.setImageResource(
                if (isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_add_to_favourites_51
            )

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
        if (viewModel.uiState.value?.playerState is PlayerState.Playing) {
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