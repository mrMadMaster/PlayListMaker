package com.example.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var trackTime: TextView
    private lateinit var artwork: ImageView
    private lateinit var collectionName: TextView
    private lateinit var releaseDate: TextView
    private lateinit var primaryGenreName: TextView
    private lateinit var country: TextView
    private lateinit var collectionNameGroup: Group
    private lateinit var releaseDateGroup: Group
    private lateinit var primaryGenreNameGroup: Group
    private lateinit var countryGroup: Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        trackName = findViewById(R.id.trackName)
        artistName = findViewById(R.id.artistName)
        trackTime = findViewById(R.id.trackTime2)
        artwork = findViewById(R.id.cover)
        collectionName = findViewById(R.id.collectionName2)
        releaseDate = findViewById(R.id.releaseDate2)
        primaryGenreName = findViewById(R.id.primaryGenreName2)
        country = findViewById(R.id.country2)
        collectionNameGroup = findViewById(R.id.collectionNameGroup)
        releaseDateGroup = findViewById(R.id.releaseDateGroup)
        primaryGenreNameGroup = findViewById(R.id.primaryGenreNameGroup)
        countryGroup = findViewById(R.id.countryGroup)

        val backButton = findViewById<ImageButton>(R.id.back)

        backButton.setOnClickListener {
            finish()
        }

        showTrack()
    }

    fun showTrack(){
        val track = intent.getSerializableExtra(TRACK_EXTRA) as? Track

        if (track != null) {
            trackName.text = track.trackName
            artistName.text = track.artistName
            trackTime.text = SimpleDateFormat(
                "mm:ss",
                Locale.getDefault()
            ).format(track.trackTimeMillis.toLong())

            if (track.collectionName.isNullOrEmpty()) {
                collectionNameGroup.visibility = View.GONE
            } else {
                collectionName.text = track.collectionName
            }

            if (track.releaseDate.isNullOrEmpty()) {
                releaseDateGroup.visibility = View.GONE
            } else {
                releaseDate.text = track.releaseDate.substring(0, 4)
            }

            if (track.primaryGenreName.isNullOrEmpty()) {
                primaryGenreNameGroup.visibility = View.GONE
            } else {
                primaryGenreName.text = track.primaryGenreName
            }

            if (track.country.isNullOrEmpty()) {
                countryGroup.visibility = View.GONE
            } else {
                country.text = track.country
            }

            Glide.with(this)
                .load(track.getCoverArtwork())
                .centerCrop()
                .placeholder(R.drawable.placeholder_312)
                .error(R.drawable.placeholder_312)
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.cover_radius)))
                .into(artwork)
        }
    }

    companion object {
        const val TRACK_EXTRA = "track_extra"
    }
}