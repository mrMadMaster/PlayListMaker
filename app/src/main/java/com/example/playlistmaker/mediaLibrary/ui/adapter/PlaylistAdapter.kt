package com.example.playlistmaker.mediaLibrary.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemPlaylistBinding
import com.example.playlistmaker.mediaLibrary.domain.models.Playlist

class PlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private var playlists: List<Playlist> = emptyList()

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
        holder.itemView.setOnClickListener {
            onPlaylistClick(playlists[position])
        }
    }

    override fun getItemCount(): Int = playlists.size

    inner class PlaylistViewHolder(
        private val binding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            with(binding) {
                playlistName.text = playlist.name
                playlistTrackCount.text = itemView.context.resources.getQuantityString(
                    R.plurals.track_count,
                    playlist.trackCount,
                    playlist.trackCount
                )

                if (!playlist.coverPath.isNullOrEmpty()) {
                    Glide.with(itemView)
                        .load(playlist.coverPath)
                        .placeholder(R.drawable.placeholder_45)
                        .error(R.drawable.placeholder_45)
                        .centerCrop()
                        .into(ivCover)
                } else {
                    ivCover.setImageResource(R.drawable.placeholder_45)
                }
            }
        }
    }
}