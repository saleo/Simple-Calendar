package com.simplemobiletools.calendar.activities

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.text.method.ScrollingMovementMethod
import android.view.ViewGroup
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.extensions.setupBottomButtonBar
import com.simplemobiletools.calendar.helpers.SKCAL_AS_DEFAULT
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.helpers.APP_VERSION_NAME
import kotlinx.android.synthetic.main.activity_about_intro.*
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import java.util.*


class AboutActivityIntro: SimpleActivity() ,TabLayout.OnTabSelectedListener{

    private var skcal_as_default:Boolean=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_intro)

        skcal_as_default=intent.getBooleanExtra(SKCAL_AS_DEFAULT,true)

        if (skcal_as_default) {
            txt_about_intro.text = getString(R.string.str_intro_skcal)
            intro_tabs.getTabAt(0)
        }
        else {
            txt_about_intro.text = getString(R.string.str_intro_sk)
            intro_tabs.getTabAt(2)
        }
        setupTabs()
        setupCopyright()
        setupBottomButtonBar(ll_intro_holder)
        txt_about_intro.movementMethod=ScrollingMovementMethod()
    }


    private fun setupTabs(){
        val tabLayout:TabLayout?=findViewById(R.id.intro_tabs)
        tabLayout!!.newTab().setText(R.string.title_intro_skcal)
        tabLayout!!.newTab().setText(R.string.title_intro_sk)
        tabLayout!!.newTab().setText(R.string.title_intro_xiangyu)

        tabLayout.addOnTabSelectedListener(this)
    }

    private fun setupCopyright() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright), year)
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
}
