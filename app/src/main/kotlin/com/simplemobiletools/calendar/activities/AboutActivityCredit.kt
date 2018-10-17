package com.simplemobiletools.calendar.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import com.simplemobiletools.calendar.helpers.SKCAL_AS_DEFAULT
import kotlinx.android.synthetic.main.activity_about_credits.*
import com.simplemobiletools.calendar.R
import com.simplemobiletools.commons.helpers.APP_VERSION_NAME
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import java.util.*

class AboutActivityCredit:SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_credits)
        btn_credit_donatenow.setOnClickListener{
            Intent(applicationContext,AboutActivityIntro::class.java).apply{
                putExtra(SKCAL_AS_DEFAULT,false)
                startActivity(this)
            }
        }

        about_sklogo.visibility=INVISIBLE
        setupCopyright()
    }

    private fun setupCopyright() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright), year)
    }
}
