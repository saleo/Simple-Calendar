package com.simplemobiletools.calendar.fragments

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.extensions.config
import com.simplemobiletools.calendar.helpers.*
import com.simplemobiletools.commons.activities.LicenseActivity
import com.simplemobiletools.commons.helpers.APP_LICENSES
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : MyFragmentHolder() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        view.background = ColorDrawable(context!!.config.backgroundColor)

        return view
    }

    override fun onResume() {
        super.onResume()
        currentDayCode=Formatter.getTodayCode(context!!)
        (activity as MainActivity).updateTopBottom(view = ABOUT_VIEW)
        setupIntro()
        setupCredit()
        setupHealth()
        setupLicense()
    }

    private fun setupIntro(){
        about_introduction_holder.setOnClickListener{
            val dt=Formatter.getDateTimeFromCode(currentDayCode)
            (activity as MainActivity).openFragmentHolder(dt, ABOUT_INTRO_VIEW)
        }
    }

    private fun setupCredit(){
        about_credit_holder.setOnClickListener{
            val dt=Formatter.getDateTimeFromCode(currentDayCode)
            (activity as MainActivity).openFragmentHolder(dt, ABOUT_CREDIT_VIEW)
        }
    }

    private fun setupHealth() {
        ll_health_holder.setOnClickListener {
            val dt=Formatter.getDateTimeFromCode(currentDayCode)
            (activity as MainActivity).openFragment(dt, ABOUT_HEALTH_VIEW)
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
