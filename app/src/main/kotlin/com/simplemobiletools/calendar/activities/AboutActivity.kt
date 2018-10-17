package com.simplemobiletools.calendar.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import com.simplemobiletools.calendar.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.activities.FAQActivity
import com.simplemobiletools.commons.activities.LicenseActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.APP_LICENSES
import com.simplemobiletools.commons.helpers.APP_NAME
import com.simplemobiletools.commons.helpers.APP_VERSION_NAME
import com.simplemobiletools.commons.models.FAQItem
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import java.util.*

class AboutActivity : BaseSimpleActivity() {
    private var appName = ""
    private var linkColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        appName = intent.getStringExtra(APP_NAME) ?: ""
        linkColor = getAdjustedPrimaryColor()
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(about_holder)

        setupIntro()
        setupCredit()
        setupShare()
        setupLicense()
        setupCopyright()
    }

    private fun setupIntro(){
        about_introduction_holder.setOnClickListener{
            Intent(applicationContext,AboutActivityIntro::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun setupCredit(){
        about_credit_holder.setOnClickListener{
            Intent(applicationContext,AboutActivityCredit::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun setupShare() {
        about_share_holder.setOnClickListener {
            val text = String.format(getString(R.string.share_text), appName, getStoreUrl())
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, appName)
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                startActivity(Intent.createChooser(this, getString(R.string.invite_via)))
            }
        }
    }

    private fun setupLicense() {
        about_license_holder.setOnClickListener {
            Intent(applicationContext, LicenseActivity::class.java).apply {
                putExtra(APP_LICENSES, intent.getIntExtra(APP_LICENSES, 0))
                startActivity(this)
            }
        }
    }


    private fun setupCopyright() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright), year)
    }

    private fun getStoreUrl() = "https://play.google.com/store/apps/details?id=$packageName"
}
