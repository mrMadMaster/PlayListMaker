package com.example.playlistmaker.search.ui.activity.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemTrackBinding
import com.example.playlistmaker.search.domain.models.Track
import java.util.Locale
import java.util.concurrent.TimeUnit

class TrackAdapter(
    private var tracks: List<Track>,
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
        holder.itemView.setOnClickListener {
            onTrackClick(tracks[position])
        }
    }

    override fun getItemCount(): Int = tracks.size

    inner class TrackViewHolder(
        private val binding: ItemTrackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track) {
            with(binding) {
                trackName.text = track.trackName
                artistName.text = track.artistName
                trackTime.text = formatTime(track.trackTimeMillis.toLong())

                Glide.with(itemView)
                    .load(track.getCoverArtwork())
                    .placeholder(R.drawable.placeholder_45)
                    .error(R.drawable.placeholder_45)
                    .centerCrop()
                    .transform(RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.cover_radius)))
                    .into(artwork)
            }
        }

        private fun formatTime(milliseconds: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                    TimeUnit.MINUTES.toSeconds(minutes)
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}