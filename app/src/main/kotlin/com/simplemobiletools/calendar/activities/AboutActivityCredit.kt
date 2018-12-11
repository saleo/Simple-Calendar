package com.simplemobiletools.calendar.activities

import android.os.Bundle
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.extensions.setupBottomButtonBar
import kotlinx.android.synthetic.main.activity_about_credits.*
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import java.util.*

class AboutActivityCredit:SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_credits)
        setupCopyright()

    }

    override fun onResume() {
        super.onResume()
        setupCopyright()
    }
    private fun setupCopyright() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright), year)
    }
}
