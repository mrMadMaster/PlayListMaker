package com.example.playlistmaker

import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class TrackViewHolder(item: View): RecyclerView.ViewHolder(item) {

    private val trackName: TextView = item.findViewById(R.id.track_name)
    private val artistName: TextView = item.findViewById(R.id.artist_name)
    private val trackTime: TextView = item.findViewById(R.id.track_time)
    private val artwork: ImageView = item.findViewById(R.id.artwork)

    fun bind(track: Track){
        trackName.text = track.trackName
        artistName.text = track.artistName
        trackTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis.toLong())
        artistName.requestLayout()

        Glide.with(itemView)
            .load(track.artworkUrl100)
            .centerCrop()
            .placeholder(R.drawable.placeholder_45)
            .transform(RoundedCorners(dpToPx(2f, itemView)))
            .into(artwork)
    }

    private fun dpToPx(dp: Float, context: View): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics).toInt()
    }
}