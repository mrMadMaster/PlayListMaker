package com.example.playlistmaker.mediaLibrary.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentNewPlayListBinding
import com.example.playlistmaker.mediaLibrary.ui.viewmodel.NewPlaylistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlayListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewPlaylistViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var originalName: String = ""
    private var originalDescription: String = ""
    private var originalImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.cover.setImageURI(it)
            binding.coverIcon.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlayListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
        saveOriginalState()
    }

    private fun setupClickListeners() {
        binding.back.setOnClickListener {
            handleBackPressed()
        }

        binding.cover.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnCreate.setOnClickListener {
            createPlaylist()
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = binding.tilName.text.toString()
                binding.btnCreate.isEnabled = name.isNotBlank()
                binding.btnCreate.backgroundTintList = if (name.isNotBlank()) {
                    resources.getColorStateList(R.color.blue, null)
                } else {
                    resources.getColorStateList(R.color.gray, null)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.tilName.addTextChangedListener(textWatcher)
        binding.tilDescription.addTextChangedListener(textWatcher)
    }

    private fun observeViewModel() {
        viewModel.createPlaylistResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.playlist_created, binding.tilName.text.toString()),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun saveOriginalState() {
        originalName = binding.tilName.text.toString()
        originalDescription = binding.tilDescription.text.toString()
        originalImageUri = selectedImageUri
    }

    private fun hasChanges(): Boolean {
        val currentName = binding.tilName.text.toString()
        val currentDescription = binding.tilDescription.text.toString()

        val nameChanged = currentName != originalName
        val descriptionChanged = currentDescription != originalDescription
        val imageChanged = selectedImageUri != originalImageUri

        val hasData = currentName.isNotBlank() ||
                currentDescription.isNotBlank() ||
                selectedImageUri != null

        return hasData && (nameChanged || descriptionChanged || imageChanged)
    }

    private fun createPlaylist() {
        val name = binding.tilName.text.toString()
        if (name.isBlank()) return

        val coverPath = selectedImageUri?.let { saveImageToAppStorage(it, name) }

        viewModel.createPlaylist(
            name = name,
            description = binding.tilDescription.text.toString(),
            coverPath = coverPath
        )
    }

    private fun saveImageToAppStorage(uri: Uri, playlistName: String): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = "playlist_${playlistName}_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, "playlist_covers")
            if (!file.exists()) file.mkdirs()

            val outputFile = File(file, fileName)
            FileOutputStream(outputFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handleBackPressed() {
        if (hasChanges()) {
            showExitConfirmationDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.finish_creating_playlist))
            .setMessage(getString(R.string.unsaved_data_will_be_lost))
            .setPositiveButton(getString(R.string.finish)) { _, _ ->
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}