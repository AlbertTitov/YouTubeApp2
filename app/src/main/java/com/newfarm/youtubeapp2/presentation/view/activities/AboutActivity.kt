package com.newfarm.youtubeapp2.presentation.view.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.lb.material_preferences_library.PreferenceActivity
import com.lb.material_preferences_library.custom_preferences.Preference

import com.newfarm.youtubeapp2.R

class AboutActivity : PreferenceActivity(), android.preference.Preference.OnPreferenceClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.AppTheme_Dark)
        super.onCreate(savedInstanceState)

        val prefShareKey = findPreference(getString(R.string.pref_share_key)) as Preference
        val prefRateReviewKey =
            findPreference(getString(R.string.pref_rate_review_key)) as Preference

        prefShareKey.onPreferenceClickListener = this
        prefRateReviewKey.onPreferenceClickListener = this
    }

    override fun getPreferencesXmlId(): Int {
        return R.xml.pref_about
    }

    override fun onPreferenceClick(preference: android.preference.Preference?): Boolean {
        if (preference?.key == getString(R.string.pref_share_key)) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            /*shareIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.subject)
            )
            shareIntent.putExtra(
                Intent.EXTRA_TEXT, getString(R.string.message) +
                        " " + getString(R.string.googleplay_url)
            )*/
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)))
        } else if (preference?.key == getString(R.string.pref_rate_review_key)) {
            val rateReviewIntent = Intent(Intent.ACTION_VIEW)
            rateReviewIntent.data = Uri.parse(
                getString(R.string.googleplay_url)
            )
            startActivity(rateReviewIntent)
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.open_main, R.anim.close_next)
    }
}