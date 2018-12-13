package com.simplemobiletools.calendar.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.adapters.MyDayPagerAdapter
import com.simplemobiletools.calendar.extensions.config
import com.simplemobiletools.calendar.helpers.DAILY_VIEW
import com.simplemobiletools.calendar.helpers.DAY_CODE
import com.simplemobiletools.calendar.helpers.Formatter
import com.simplemobiletools.calendar.interfaces.NavigationListener
import com.simplemobiletools.commons.views.MyViewPager
import kotlinx.android.synthetic.main.fragment_day.view.*
import kotlinx.android.synthetic.main.fragment_days_holder.view.*
import org.joda.time.DateTime
import java.util.*

class HealthFragmentsHolder : MyFragmentHolder() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentDayCode = arguments?.getString(DAY_CODE) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_days_holder, container, false)
        view.background = ColorDrawable(context!!.config.backgroundColor)
        return view
    }


    private fun setupFragment() {
        val codes = getDays(currentDayCode)

        updateActionBarTitle()
    }

    private fun getDays(code: String): List<String> {
        val days = ArrayList<String>(1)
        return days
    }

    override fun goToToday() {
    }

    override fun refreshEvents() {
    }

    override fun shouldGoToTodayBeVisible(): Boolean{
        return true
    }

    override fun updateActionBarTitle() {
    }

    override fun getNewEventDayCode(): String{
        return ""
    }
}
