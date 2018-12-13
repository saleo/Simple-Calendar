package com.simplemobiletools.calendar.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.activities.SimpleActivity
import com.simplemobiletools.calendar.extensions.config
import com.simplemobiletools.calendar.helpers.ABOUT_CREDIT_VIEW
import com.simplemobiletools.calendar.helpers.ABOUT_INTRO_VIEW
import com.simplemobiletools.calendar.helpers.Formatter
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import java.util.*

class CreditFragment: MyFragmentHolder() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_about_credits,container,false)
        view.background = ColorDrawable(context!!.config.backgroundColor)
        return view
    }

    override fun onResume() {
        super.onResume()
        currentDayCode= Formatter.getTodayCode(context!!)
        (activity as MainActivity).updateTopBottom(view = ABOUT_CREDIT_VIEW)
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
