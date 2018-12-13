package com.simplemobiletools.calendar.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.activities.SimpleActivity
import com.simplemobiletools.calendar.extensions.config
import com.simplemobiletools.calendar.helpers.ABOUT_INTRO_VIEW
import com.simplemobiletools.calendar.helpers.ABOUT_VIEW
import com.simplemobiletools.calendar.helpers.Formatter
import com.simplemobiletools.calendar.helpers.SKCAL_AS_DEFAULT
import kotlinx.android.synthetic.main.fragment_about_intro.*
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import java.util.*


class IntroFragment: MyFragmentHolder() ,TabLayout.OnTabSelectedListener{

    private var skcal_as_default:Boolean=true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_about_intro,container,false)
        view.background = ColorDrawable(context!!.config.backgroundColor)
        return view
    }

    override fun onResume() {
        super.onResume()
        currentDayCode= Formatter.getTodayCode(context!!)
        (activity as MainActivity).updateTopBottom(view = ABOUT_INTRO_VIEW)

        txt_about_intro.text = getString(R.string.str_intro_skcal)
        intro_tabs.getTabAt(0)

        setupTabs()
        txt_about_intro.movementMethod=ScrollingMovementMethod()
    }

    private fun setupTabs(){
        val tabLayout:TabLayout?=view!!.findViewById(R.id.intro_tabs)
        tabLayout!!.newTab().setText(R.string.title_intro_skcal)
        tabLayout.newTab().setText(R.string.title_intro_sk)
        tabLayout.newTab().setText(R.string.title_intro_xiangyu)

        tabLayout.addOnTabSelectedListener(this)
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab!!.text){
            getString(R.string.title_intro_skcal)-> txt_about_intro.text=getString(R.string.str_intro_skcal)
            getString(R.string.title_intro_sk)-> txt_about_intro.text=getString(R.string.str_intro_sk)
            getString(R.string.title_intro_xiangyu)-> txt_about_intro.text=getString(R.string.str_intro_xiangyu)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        return
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        return
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
