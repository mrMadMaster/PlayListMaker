package com.example.playlistmaker.mediaLibrary.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavoritesBinding
import com.example.playlistmaker.mediaLibrary.ui.viewmodel.FavoritesState
import com.example.playlistmaker.mediaLibrary.ui.viewmodel.FavoritesViewModel
import com.example.playlistmaker.player.ui.fragments.AudioPlayerFragment
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.ui.adapter.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesViewModel by viewModel()

    private lateinit var favoritesAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupObservers()
    }

    private fun setupAdapter() {
        favoritesAdapter = TrackAdapter(emptyList()) { track ->
            navigateToPlayer(track)
        }

        binding.favoritesRecyclerView.apply {
            adapter = favoritesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            renderState(state)
        }
    }

    private fun renderState(state: FavoritesState) {
        with(binding) {
            when (state) {
                is FavoritesState.Empty -> {
                    emptyStateLayout.isVisible = true
                    favoritesRecyclerView.isVisible = false
                }
                is FavoritesState.Content -> {
                    emptyStateLayout.isVisible = false
                    favoritesRecyclerView.isVisible = true
                    favoritesAdapter.updateTracks(state.tracks)
                }
            }
        }
    }

    private fun navigateToPlayer(track: Track) {
        val bundle = AudioPlayerFragment.createArguments(track)
        findNavController().navigate(R.id.audioPlayerFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}