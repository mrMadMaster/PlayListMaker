package com.example.playlistmaker.search.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.player.ui.activity.AudioPlayerActivity
import com.example.playlistmaker.player.ui.activity.AudioPlayerActivity.Companion.TRACK_EXTRA_KEY
import com.example.playlistmaker.search.domain.models.SearchState
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.ui.activity.adapter.TrackAdapter
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModel()

    private lateinit var searchAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }

        binding.tracksHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }
    }

    private fun setupViews() {

        binding.back.setOnClickListener { finish() }

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
        viewModel.searchState.observe(this) { state ->
            handleSearchState(state)
        }

        viewModel.searchHistory.observe(this) { history ->
            historyAdapter.updateTracks(history)
            updateHistoryVisibility(history)
        }

        viewModel.isSearching.observe(this) { isSearching ->
            binding.progressBar.isVisible = isSearching
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
            viewModel.search(query)
        }
    }

    private fun onTrackClick(track: Track) {
        viewModel.addToSearchHistory(track)
        hideKeyboard()
        startActivity(
            Intent(this, AudioPlayerActivity::class.java)
                .putExtra(TRACK_EXTRA_KEY, track)
        )
    }

    private fun loadSearchHistory() {
        viewModel.loadSearchHistory()
    }

    private fun showSearchHistory() {
        binding.searchHistory.isVisible = true
        binding.tracks.isVisible = false
        binding.nothingFound.isVisible = false
        binding.noConnection.isVisible = false
    }

    private fun updateHistoryVisibility(history: List<Track>) {
        binding.searchHistory.isVisible = history.isNotEmpty() &&
                binding.searchText.text.isNullOrEmpty()
    }

    private fun updateClearButtonVisibility(text: CharSequence?) {
        binding.clear.isVisible = !text.isNullOrEmpty()
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(binding.searchText.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelSearch()
    }
}