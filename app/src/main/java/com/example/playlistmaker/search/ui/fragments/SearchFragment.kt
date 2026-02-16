package com.example.playlistmaker.search.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.player.ui.fragments.AudioPlayerFragment
import com.example.playlistmaker.search.domain.models.SearchState
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.ui.adapter.TrackAdapter
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()

    private lateinit var searchAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupViews()
        setupObservers()
        loadSearchHistory()
    }

    private fun setupAdapters() {
        searchAdapter = TrackAdapter(emptyList()) { track ->
            onTrackClick(track)
        }

        historyAdapter = TrackAdapter(emptyList()) { track ->
            onTrackClick(track)
        }

        binding.tracks.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.tracksHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupViews() {

        binding.searchText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                true
            } else {
                false
            }
        }

        binding.clear.setOnClickListener {
            binding.searchText.text.clear()
            viewModel.cancelSearch()
            showSearchHistory()
        }

        binding.refreshButton.setOnClickListener {
            performSearch()
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearSearchHistory()
        }

        binding.searchText.doOnTextChanged { text, _, _, _ ->
            updateClearButtonVisibility(text)
            if (text.isNullOrEmpty()) {
                showSearchHistory()
            } else {
                viewModel.searchDebounced(text.toString())
            }
        }
    }

    private fun setupObservers() {

        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            if (_binding != null) {
                handleSearchState(state)
            }
        }

        viewModel.searchHistory.observe(viewLifecycleOwner) { history ->
            if (_binding != null) {
                historyAdapter.updateTracks(history)
                updateHistoryVisibility(history)
            }
        }

        viewModel.isSearching.observe(viewLifecycleOwner) { isSearching ->
            if (_binding != null) {
                binding.progressBar.isVisible = isSearching
            }
        }

        binding.root.setOnClickListener {
            hideKeyboard()
        }
    }

    private fun handleSearchState(state: SearchState) {
        with(binding) {
            when (state) {
                is SearchState.Empty -> {
                    showSearchHistory()
                }
                is SearchState.Loading -> {
                    tracks.isVisible = false
                    nothingFound.isVisible = false
                    noConnection.isVisible = false
                }
                is SearchState.EmptyResult -> {
                    tracks.isVisible = false
                    nothingFound.isVisible = true
                    noConnection.isVisible = false
                    searchHistory.isVisible = false
                }
                is SearchState.Content -> {
                    searchAdapter.updateTracks(state.tracks)
                    tracks.isVisible = true
                    nothingFound.isVisible = false
                    noConnection.isVisible = false
                    searchHistory.isVisible = false
                }
                is SearchState.Error.NoConnection -> {
                    tracks.isVisible = false
                    nothingFound.isVisible = false
                    noConnection.isVisible = true
                    searchHistory.isVisible = false
                }
                is SearchState.Error.NetworkError -> {
                    tracks.isVisible = false
                    nothingFound.isVisible = false
                    noConnection.isVisible = true
                    searchHistory.isVisible = false
                }
            }
        }
    }

    private fun performSearch() {
        hideKeyboard()
        val query = binding.searchText.text.toString()
        if (query.isNotEmpty()) {
            viewModel.searchDebounced(query)
        }
    }

    private fun onTrackClick(track: Track) {
        viewModel.clickDebounced(track)
        hideKeyboard()

        val bundle = AudioPlayerFragment.createArguments(track)
         findNavController().navigate(R.id.audioPlayerFragment, bundle)
    }

    private fun loadSearchHistory() {
        viewModel.loadSearchHistory()
    }

    private fun showSearchHistory() {
        with(binding) {
            searchHistory.isVisible = true
            tracks.isVisible = false
            nothingFound.isVisible = false
            noConnection.isVisible = false
        }
    }

    private fun updateHistoryVisibility(history: List<Track>) {
        binding.searchHistory.isVisible = history.isNotEmpty() &&
                binding.searchText.text.isNullOrEmpty()
    }

    private fun updateClearButtonVisibility(text: CharSequence?) {
        binding.clear.isVisible = !text.isNullOrEmpty()
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext()
            .getSystemService(InputMethodManager::class.java)
        inputMethodManager?.hideSoftInputFromWindow(binding.searchText.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.cancelSearch()
        _binding = null
    }
}