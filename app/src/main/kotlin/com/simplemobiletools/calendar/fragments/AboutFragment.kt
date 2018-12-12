package com.simplemobiletools.calendar.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.AboutActivityCredit
import com.simplemobiletools.calendar.activities.AboutActivityHealth
import com.simplemobiletools.calendar.activities.AboutActivityIntro
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.helpers.ABOUT_VIEW
import com.simplemobiletools.commons.activities.LicenseActivity
import com.simplemobiletools.commons.helpers.APP_LICENSES
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import java.util.*

class AboutFragment : MyFragmentHolder() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_about, container, false)

        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).updateTopBottom(view = ABOUT_VIEW)
        setupIntro()
        setupCredit()
        setupHealth()
        setupLicense()
    }

    private fun setupIntro(){
        about_introduction_holder.setOnClickListener{
            Intent(context, AboutActivityIntro::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun setupCredit(){
        about_credit_holder.setOnClickListener{
            Intent(context, AboutActivityCredit::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun setupHealth() {
        ll_health_holder.setOnClickListener {
            Intent(context, AboutActivityHealth::class.java).apply {
                startActivity(this)
            }
        }
    }


    private fun setupLicense() {
        about_license_holder.setOnClickListener {
            Intent(context, LicenseActivity::class.java).apply {
                putExtra(APP_LICENSES, this.getIntExtra(APP_LICENSES, 0))
                startActivity(this)
            }
        }
    }

    //all below are for placeholder purpose
    override fun goToToday() {
    }

    override fun refreshEvents() {
    }

    override fun shouldGoToTodayBeVisible(): Boolean {
        return true
    }

    override fun updateActionBarTitle() {
    }


    override fun getNewEventDayCode(): String {
        return ""
    }
}
