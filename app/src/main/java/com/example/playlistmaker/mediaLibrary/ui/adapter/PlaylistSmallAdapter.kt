package com.example.playlistmaker.mediaLibrary.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemPlaylistSmallBinding
import com.example.playlistmaker.mediaLibrary.domain.models.Playlist
import com.example.playlistmaker.utils.TrackInfoFormatter

class PlaylistSmallAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistSmallAdapter.PlaylistSmallViewHolder>() {

    private var playlists: List<Playlist> = emptyList()

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistSmallViewHolder {
        val binding = ItemPlaylistSmallBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistSmallViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistSmallViewHolder, position: Int) {
        holder.bind(playlists[position])
        holder.itemView.setOnClickListener {
            onPlaylistClick(playlists[position])
        }
    }

    override fun getItemCount(): Int = playlists.size

    inner class PlaylistSmallViewHolder(
        private val binding: ItemPlaylistSmallBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            with(binding) {
                playlistName2.text = playlist.name
                playlistTrackCount2.text = TrackInfoFormatter.getTrackCountText(playlist.trackCount)

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