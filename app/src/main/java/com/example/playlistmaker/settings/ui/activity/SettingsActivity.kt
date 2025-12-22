package com.example.playlistmaker.settings.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleDarkTheme(isChecked)
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

        binding.back.setOnClickListener { finish() }
    }

    private fun setupObservers() {
        viewModel.themeState.observe(this) { isDarkTheme ->
            binding.themeSwitcher.isChecked = isDarkTheme
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadThemeState()
    }
}