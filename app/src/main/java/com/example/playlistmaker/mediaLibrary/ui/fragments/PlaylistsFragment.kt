package com.example.playlistmaker.mediaLibrary.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.mediaLibrary.ui.adapter.PlaylistAdapter
import com.example.playlistmaker.mediaLibrary.ui.viewmodel.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter { playlist ->

        }

        binding.playlistsRecyclerView.apply {
            adapter = playlistAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setupClickListeners() {
        binding.buttonNewPlaylist.setOnClickListener {
            findNavController().navigate(R.id.newPlaylistFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            if (playlists.isEmpty()) {
                binding.emptyStateLayout.isVisible = true
                binding.playlistsRecyclerView.isVisible = false
            } else {
                binding.emptyStateLayout.isVisible = false
                binding.playlistsRecyclerView.isVisible = true
                playlistAdapter.updatePlaylists(playlists)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}