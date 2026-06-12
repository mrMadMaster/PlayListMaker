package com.example.playlistmaker.search.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.player.ui.fragments.AudioPlayerFragment
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.ui.SearchScreen
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModel
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModel()
    private val settingsViewModel: SettingsViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SearchScreen(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    onTrackClick = { track -> navigateToPlayer(track) }
                )
            }
        }
    }

    private fun navigateToPlayer(track: Track) {
        val bundle = AudioPlayerFragment.createArguments(track)
        findNavController().navigate(R.id.audioPlayerFragment, bundle)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSearchHistory()
        viewModel.restoreSearchText()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.cancelSearch()
    }
}