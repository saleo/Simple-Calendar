package com.simplemobiletools.calendar.activities

import android.content.Intent
import android.os.Bundle
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.extensions.setupBottomButtonBar
import com.simplemobiletools.commons.activities.LicenseActivity
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.APP_LICENSES
import com.simplemobiletools.commons.helpers.APP_NAME
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import java.util.*

class AboutActivity : SimpleActivity() {
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
        updateTextColors(rl_about_holder)

        setupIntro()
        setupCredit()
        setupHealth()
        setupLicense()
        setupCopyright()
        setupBottomButtonBar(rl_about_holder)
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

    private fun setupHealth() {
        ll_health_holder.setOnClickListener {
            Intent(applicationContext,AboutActivityHealth::class.java).apply {
                startActivity(this)
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


}
