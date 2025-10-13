package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<ImageButton>(R.id.back)
        backButton.setOnClickListener { finish() }

        val shareButton = findViewById<RelativeLayout>(R.id.button_share)
        shareButton.setOnClickListener {
            val message = resources.getString(R.string.practicum)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type="text/url"
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(shareIntent)
        }

        val supportButton = findViewById<RelativeLayout>(R.id.button_support)
        supportButton.setOnClickListener {
            val supportIntent = Intent(Intent.ACTION_SENDTO)
            supportIntent.data = "mailto:".toUri()
            supportIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
            supportIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.theme_message_to_developer))
            supportIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message_to_developer))
            startActivity(supportIntent)
        }

        val agreementButton = findViewById<RelativeLayout>(R.id.button_user_agreement)
        agreementButton.setOnClickListener {
            val agreementIntent = Intent(Intent.ACTION_VIEW,
                getString(R.string.practicum_offer_ru).toUri())
            startActivity(agreementIntent)
        }

        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)
        themeSwitcher.isChecked = (applicationContext as App).getCurrentTheme()
        themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            (applicationContext as App).switchTheme(checked)
        }
    }
}