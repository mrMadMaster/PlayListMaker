package com.example.playlistmaker.player.ui.fragments

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
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
import com.example.playlistmaker.player.data.PlayerService
import com.example.playlistmaker.player.domain.service.PlayerServiceConnection
import com.example.playlistmaker.player.domain.models.PlaybackProgress
import com.example.playlistmaker.player.ui.custom.PlaybackButtonView
import com.example.playlistmaker.player.ui.viewmodel.PlayerUiState
import com.example.playlistmaker.player.ui.viewmodel.PlayerViewModel
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.utils.CustomSnackbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch

class AudioPlayerFragment : Fragment() {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerViewModel by viewModel()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var playlistAdapter: PlaylistSmallAdapter

    private var serviceConnection: ServiceConnection? = null
    private var boundService: PlayerServiceConnection? = null
    private var track: Track? = null

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATIONS
                )
            }
        }

        track = arguments?.getParcelable<Track>(TRACK_ARG)
        if (track == null) {
            findNavController().popBackStack()
            return
        }

        displayTrackInfo(track!!)
        setupClickListeners()
        setupBottomSheet()
        observeViewModel()

        bindPlayerService()
    }

    private fun bindPlayerService() {
        val intent = Intent(requireContext(), PlayerService::class.java).apply {
            putExtra(PlayerService.EXTRA_ARTIST, track?.artistName ?: "")
            putExtra(PlayerService.EXTRA_TRACK_NAME, track?.trackName ?: "")
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val localBinder = binder as? PlayerService.LocalBinder
                val service = localBinder?.getService()
                if (service != null) {
                    boundService = service
                    viewModel.onServiceConnected(service)
                    viewModel.setupTrack(track!!)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                boundService = null
            }
        }
        requireContext().bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
    }

    private fun displayTrackInfo(track: Track) {
        with(binding) {
            trackName.text = track.trackName
            artistName.text = track.artistName
            val durationMillis = track.trackTimeMillis.toIntOrNull() ?: 0
            trackTime2.text = PlaybackProgress.formatTime(durationMillis)

            Glide.with(requireContext())
                .load(track.getCoverArtwork())
                .centerCrop()
                .placeholder(R.drawable.placeholder_312)
                .error(R.drawable.placeholder_312)
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.cover_radius)))
                .into(cover)

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
        binding.playbackButton.setOnPlaybackClickListener(object : PlaybackButtonView.OnPlaybackClickListener {
            override fun onPlaybackClick() {
                viewModel.togglePlayback()
            }
        })
        binding.toFavourites.setOnClickListener {
            viewModel.onFavoriteClicked()
        }
        binding.toPlaylist.setOnClickListener {
            showPlaylistBottomSheet()
        }
    }

    private fun setupBottomSheet() {
        val bottomSheet = binding.playlistsBottomSheet.root
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
            peekHeight = (505 * resources.displayMetrics.density).toInt()
        }
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (!isAdded || _binding == null) return
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    binding.overlay.visibility = View.GONE
                    binding.playlistsBottomSheet.root.visibility = View.GONE
                } else {
                    binding.overlay.visibility = View.VISIBLE
                    binding.playlistsBottomSheet.root.visibility = View.VISIBLE
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        playlistAdapter = PlaylistSmallAdapter { playlist -> onPlaylistSelected(playlist) }
        binding.playlistsBottomSheet.playlistsRecyclerView.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.playlistsBottomSheet.btnNewPlaylist.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(
                AudioPlayerFragmentDirections.actionAudioPlayerFragmentToNewPlaylistFragment()
            )
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
        track?.let { viewModel.addTrackToPlaylist(playlist, it) }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        val isFav = viewModel.isFavorite.value
                        renderUiState(uiState, isFav)
                    }
                }
                launch {
                    viewModel.isFavorite.collect { isFav ->
                        val ui = viewModel.uiState.value
                        renderUiState(ui, isFav)
                    }
                }
                launch {
                    viewModel.playlists.collect { playlists ->
                        playlistAdapter.updatePlaylists(playlists)
                    }
                }
                launch {
                    viewModel.addToPlaylistResult.collect { result ->
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
            }
        }
    }

    private fun renderUiState(state: PlayerUiState, isFavorite: Boolean) {
        android.util.Log.d("AudioPlayer", "render: isPlaying = ${state.isPlayButtonPlaying}")
        with(binding) {
            remainingTime.text = state.currentTime
            toFavourites.setImageResource(
                if (isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_add_to_favourites_51
            )
            playbackButton.setPlaybackState(state.isPlayButtonPlaying)
            remainingTime.visibility = if (state.showProgress) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppBackgroundChanged(false)
    }

    override fun onPause() {
        super.onPause()
        if (!isRemoving) {
            viewModel.onAppBackgroundChanged(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        serviceConnection?.let {
            requireContext().unbindService(it)
        }
        _binding = null
    }

    companion object {
        const val TRACK_ARG = "track"
        fun createArguments(track: Track): Bundle = Bundle().apply { putParcelable(TRACK_ARG, track) }
        private const val REQUEST_CODE_NOTIFICATIONS = 1001
    }
}