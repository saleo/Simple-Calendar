package com.simplemobiletools.calendar.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.simplemobiletools.calendar.helpers.SKCAL_AS_DEFAULT
import kotlinx.android.synthetic.main.activity_about_credits.*
import com.simplemobiletools.calendar.R

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
    }
}
