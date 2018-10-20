package com.simplemobiletools.calendar.activities

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.widget.ListAdapter
import android.widget.SimpleAdapter
import com.simplemobiletools.calendar.helpers.SKCAL_AS_DEFAULT
import kotlinx.android.synthetic.main.activity_about_credits.*
import com.simplemobiletools.calendar.R
import com.simplemobiletools.commons.helpers.APP_VERSION_NAME
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import java.util.*
import kotlin.collections.LinkedHashMap
import com.simplemobiletools.calendar.helpers.HEALTH_TITLE
import com.simplemobiletools.calendar.helpers.HEALTH_CONTENT
import com.simplemobiletools.calendar.helpers.HEALTH_CONTENT2
import kotlinx.android.synthetic.main.activity_about_health.*
import kotlinx.android.synthetic.main.health_list_header.view.*
import org.apache.commons.lang3.ObjectUtils


class AboutActivityHealth: ListActivity(),TabLayout.OnTabSelectedListener {
    private lateinit var mHeader : View
    private var mOldHeader : View?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_health)

        about_sklogo.visibility=INVISIBLE
        setupTabs()
        mHeader= inflate(applicationContext,R.layout.health_list_header,null)

        setupListview()
        setupCopyright()
    }

    private fun setupTabs(){
        val tabLayout: TabLayout?=findViewById(R.id.health_tabs)
        tabLayout!!.newTab().setText(R.string.str_intro)
        tabLayout!!.newTab().setText(R.string.str_intro)
        tabLayout!!.newTab().setText(R.string.str_intro)

        tabLayout.addOnTabSelectedListener(this)
    }

    private fun setupListview(tabSelected:Int=0) {
        var lst1: List<Map<String, String>> = emptyList()
        var sa1: Array<String> = emptyArray()
        var item1: Map<String, String> = emptyMap()
        var iLen = 0

        when (tabSelected) {
            0 -> {
                sa1 = resources.getStringArray(R.array.health1)
                iLen = sa1.size / 3
                mHeader?.header_health_title.text=resources.getString(R.string.str_health_headerviewtext1)
                mHeader?.header_health_content?.text=resources.getString(R.string.str_health_headerviewtext1_1)
                txt_health_originator.visibility= INVISIBLE
            }
            1 -> {
                sa1 = resources.getStringArray(R.array.health2)
                iLen = sa1.size / 2
                mHeader!!.header_health_title.text=resources.getString(R.string.str_health_headerviewtext2)
                mHeader!!.header_health_content.text=resources.getString(R.string.str_health_headerviewtext2_1)
                txt_health_originator.visibility= INVISIBLE
            }
            2 -> {
                sa1 = resources.getStringArray(R.array.health3)
                iLen = sa1.size / 2
                mHeader!!.header_health_title.text=resources.getString(R.string.str_health_headerviewtext3)
                mHeader!!.header_health_content.text==resources.getString(R.string.str_health_headerviewtext3_1)
                txt_health_originator.visibility= VISIBLE
            }
        }

        if (mOldHeader!=null)
            listView.removeHeaderView(mOldHeader)

        listView.addHeaderView(mHeader)
        mOldHeader=mHeader

        if (tabSelected != 0) {
            for (i in 0 until iLen) {
                item1 = mapOf(HEALTH_TITLE to sa1[i * 2], HEALTH_CONTENT to sa1[i * 2 + 1])
                lst1 = lst1.plus(item1)
            }
        } else {
            for (i in 0 until iLen) {
                item1 = mapOf(HEALTH_TITLE to sa1[i * 3], HEALTH_CONTENT to sa1[i * 3 + 1], HEALTH_CONTENT2 to sa1[i * 3 + 2])
                lst1 = lst1.plus(item1)
            }
        }


        if (tabSelected != 0)
            listAdapter = SimpleAdapter(applicationContext, lst1, R.layout.health_list_item, arrayOf(HEALTH_TITLE, HEALTH_CONTENT), intArrayOf(R.id.item_health_title, R.id.item_health_content))
        else
            listAdapter = SimpleAdapter(applicationContext, lst1, R.layout.health_list_item2, arrayOf(HEALTH_TITLE, HEALTH_CONTENT,HEALTH_CONTENT2), intArrayOf(R.id.item_health_title, R.id.item_health_content, R.id.item_health_content2))

    }

    private fun setupCopyright() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright), year)
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        setupListview(tab!!.position)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        return
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        return
    }
}
