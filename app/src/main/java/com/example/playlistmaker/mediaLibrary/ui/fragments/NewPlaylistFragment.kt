package com.example.playlistmaker.mediaLibrary.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.DialogConfirmationBinding
import com.example.playlistmaker.databinding.FragmentNewPlayListBinding
import com.example.playlistmaker.mediaLibrary.ui.viewmodel.NewPlaylistViewModel
import com.example.playlistmaker.utils.CustomSnackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import androidx.core.net.toUri

class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlayListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewPlaylistViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var originalName: String = ""
    private var originalDescription: String = ""
    private var originalImageUri: Uri? = null
    private var playlistId: Int = 0
    private var isEditMode: Boolean = false

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

        playlistId = arguments?.getInt("playlist_id", 0) ?: 0
        isEditMode = playlistId != 0

        if (isEditMode) {
            binding.title.text = getString(R.string.edit_playlist_title)
            binding.btnCreate.text = getString(R.string.save_playlist)
            viewModel.loadPlaylistForEdit(playlistId)
        } else {
            binding.title.text = getString(R.string.new_playlist_title)
            binding.btnCreate.text = getString(R.string.create_playlist)
        }

        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
        saveOriginalState()
        setupBackPressedCallback()
    }

    private fun setupClickListeners() {
        binding.back.setOnClickListener {
            handleBackPressed()
        }

        binding.cover.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnCreate.setOnClickListener {
            savePlaylist()
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
            if (success == true) {
                val playlistName = binding.tilName.text.toString()
                CustomSnackbar.show(
                    binding.root,
                    getString(R.string.playlist_created, playlistName)
                )
                viewModel.clearResults()
                findNavController().popBackStack()
            }
        }

        viewModel.editPlaylistResult.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                viewModel.clearResults()
                findNavController().popBackStack()
            }
        }

        viewModel.playlistForEdit.observe(viewLifecycleOwner) { playlist ->
            if (isEditMode && playlist != null) {
                fillPlaylistData(playlist)
            }
        }
    }

    private fun fillPlaylistData(playlist: com.example.playlistmaker.mediaLibrary.domain.models.Playlist) {
        binding.tilName.setText(playlist.name)
        binding.tilDescription.setText(playlist.description ?: "")

        if (!playlist.coverPath.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(playlist.coverPath)
                .into(binding.cover)
            binding.coverIcon.visibility = View.GONE
            selectedImageUri = playlist.coverPath.toUri()
        }

        originalName = playlist.name
        originalDescription = playlist.description ?: ""
        originalImageUri = selectedImageUri
    }

    private fun savePlaylist() {
        val name = binding.tilName.text.toString()
        if (name.isBlank()) return

        val coverPath = selectedImageUri?.let { saveImageToAppStorage(it, name) }

        if (isEditMode) {
            viewModel.updatePlaylist(
                id = playlistId,
                name = name,
                description = binding.tilDescription.text.toString(),
                coverPath = coverPath
            )
        } else {
            viewModel.createPlaylist(
                name = name,
                description = binding.tilDescription.text.toString(),
                coverPath = coverPath
            )
        }
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

    private fun handleBackPressed() {
        if (isEditMode) {
            findNavController().popBackStack()
        } else if (hasChanges()) {
            showExitConfirmationDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun setupBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackPressed()
                }
            }
        )
    }

    private fun showExitConfirmationDialog() {
        val dialogBinding = DialogConfirmationBinding.inflate(layoutInflater)

        dialogBinding.tvTitle.text = getString(R.string.exit_playlist_creation)
        dialogBinding.tvTitle.visibility = View.VISIBLE

        dialogBinding.tvMessage.text = getString(R.string.unsaved_data_warning)
        dialogBinding.tvMessage.visibility = View.VISIBLE

        dialogBinding.btnCancel.visibility = View.VISIBLE
        dialogBinding.btnFinish.visibility = View.VISIBLE

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnFinish.setOnClickListener {
            findNavController().popBackStack()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}