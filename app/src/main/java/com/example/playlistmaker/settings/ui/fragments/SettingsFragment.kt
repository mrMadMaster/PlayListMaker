package com.example.playlistmaker.settings.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            if (binding.themeSwitcher.isPressed) {
                viewModel.toggleDarkTheme(isChecked)
            }
        }

        binding.send.setOnClickListener {
            viewModel.shareApp()
        }

        binding.support.setOnClickListener {
            viewModel.openSupport()
        }

        binding.agreement.setOnClickListener {
            viewModel.openUserAgreement()
        }
    }

    private fun setupObservers() {
        viewModel.themeState.observe(viewLifecycleOwner) { isDarkTheme ->
            binding.themeSwitcher.isChecked = isDarkTheme
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadThemeState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}