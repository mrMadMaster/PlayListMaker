package com.example.playlistmaker.sharing.data.provider

import android.content.Context
import com.example.playlistmaker.R
import com.example.playlistmaker.sharing.domain.model.EmailData

class ResourceSharingConfigProvider(
    private val context: Context
) : SharingConfigProvider {

    override fun getSharingConfig(): EmailData {
        return EmailData(
            playStoreUrl = context.getString(R.string.practicum),
            userAgreementUrl = context.getString(R.string.practicum_offer_ru),
            supportEmail = context.getString(R.string.email),
            message = context.getString(R.string.message_to_developer),
            messageTitle = context.getString(R.string.theme_message_to_developer)
        )
    }
}