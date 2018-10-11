package com.simplemobiletools.calendar.activities

import android.os.Bundle
import android.support.design.widget.TabItem
import android.support.design.widget.TabLayout
import android.view.ViewGroup
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.fragments.IntroFragment
import com.simplemobiletools.calendar.helpers.INTRO_TYPE
import com.simplemobiletools.calendar.helpers.SKCAL_AS_DEFAULT
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.toast
import kotlinx.android.synthetic.main.activity_about_intro.*
import kotlinx.android.synthetic.main.bottom_copyright.*


class AboutActivityIntro: BaseSimpleActivity() ,TabLayout.OnTabSelectedListener{

    private var skcal_as_default:Boolean=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_intro)

        skcal_as_default=intent.getBooleanExtra(SKCAL_AS_DEFAULT,true)

        if (skcal_as_default)
            txt_about_intro.text=getString(R.string.str_intro_skcal)
        else
            txt_about_intro.text=getString(R.string.str_intro_donate)
        setupTabs()
    }

    override fun onResume() {
        super.onResume()
        about_intro_activity_holder.measure(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        about_topimage.measure(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        intro_tabs.measure(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        about_copyright_holder.measure(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        val i:Int=about_intro_activity_holder.measuredHeight-about_topimage.measuredHeight-intro_tabs.measuredHeight-about_copyright_holder.measuredHeight
        txt_about_intro.height=i
    }

    private fun setupTabs(){
        val tabLayout:TabLayout?=findViewById(R.id.intro_tabs)
        tabLayout!!.newTab().setText(R.string.title_intro_skcal)
        tabLayout!!.newTab().setText(R.string.title_intro_sk)
        tabLayout!!.newTab().setText(R.string.title_intro_donate)
        tabLayout!!.newTab().setText(R.string.title_intro_xiangyu)

        tabLayout.addOnTabSelectedListener(this)
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab!!.text){
            getString(R.string.title_intro_skcal)-> txt_about_intro.text=getString(R.string.str_intro_skcal)
            getString(R.string.title_intro_sk)-> txt_about_intro.text=getString(R.string.str_intro_sk)
            getString(R.string.title_intro_donate)-> txt_about_intro.text=getString(R.string.str_intro_donate)
            getString(R.string.title_intro_xiangyu)-> txt_about_intro.text=getString(R.string.str_intro_xiangyu)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        return
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        return
    }
}
