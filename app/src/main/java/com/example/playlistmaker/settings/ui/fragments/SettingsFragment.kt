package com.example.playlistmaker.settings.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.playlistmaker.settings.ui.SettingsScreen
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import org.koin.android.ext.android.inject

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadThemeState()
    }
}