package com.simplemobiletools.calendar.activities

import android.app.ListActivity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.widget.*
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


class AboutActivityHealth: ListActivity(),RadioGroup.OnCheckedChangeListener {
    private lateinit var mHeader: View
    private var mOldHeader: View? = null
    private var mBorder: Drawable? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_health)

        mBorder=resources.getDrawable(R.drawable.divider_green)
        val h=mBorder!!.intrinsicHeight
        val w= mBorder!!.intrinsicWidth
        mBorder!!.setBounds(0,0,w,h)

        mHeader = inflate(applicationContext, R.layout.health_list_header, null)

        setupTabs()
        setupListview()
        setupCopyright()

    }

    private fun setupTabs() {
        rdg_health_tabs.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when (checkedId) {
            R.id.rd_health_tab1 ->
                setupListview(0)
            R.id.rd_health_tab2 ->
                setupListview(1)
            R.id.rd_health_tab3 ->
                setupListview(2)
        }
    }

    private fun setupListview(tabSelected: Int = 0) {
        var lst1: List<Map<String, String>> = emptyList()
        var sa1: Array<String> = emptyArray()
        var item1: Map<String, String> = emptyMap()
        var iLen = 0

        when (tabSelected) {
            0 -> {
                sa1 = resources.getStringArray(R.array.health1)
                iLen = sa1.size / 3
                mHeader?.header_health_title.text = resources.getString(R.string.str_health_headerviewtext1)
                mHeader?.header_health_content?.text = resources.getString(R.string.str_health_headerviewtext1_1)
            }
            1 -> {
                sa1 = resources.getStringArray(R.array.health2)
                iLen = sa1.size / 2
                mHeader!!.header_health_title.text = resources.getString(R.string.str_health_headerviewtext2)
                mHeader!!.header_health_content.text = resources.getString(R.string.str_health_headerviewtext2_1)
            }
            2 -> {
                sa1 = resources.getStringArray(R.array.health3)
                iLen = sa1.size / 2
                mHeader!!.header_health_title.text = resources.getString(R.string.str_health_headerviewtext3)
                mHeader!!.header_health_content.text == resources.getString(R.string.str_health_headerviewtext3_1)
            }
        }
        setBottomBorder(tabSelected)

        if (mOldHeader != null)
            listView.removeHeaderView(mOldHeader)

        listView.addHeaderView(mHeader)
        mOldHeader = mHeader

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
            listAdapter = SimpleAdapter(applicationContext, lst1, R.layout.health_list_item2, arrayOf(HEALTH_TITLE, HEALTH_CONTENT, HEALTH_CONTENT2), intArrayOf(R.id.item_health_title, R.id.item_health_content, R.id.item_health_content2))

    }

    private fun setupCopyright() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright), year)
    }

    private fun setBottomBorder(tabIndex: Int=0){
        when (tabIndex){
            0 -> {
                rd_health_tab1.setCompoundDrawablesWithIntrinsicBounds(null, null, null,mBorder)
                rd_health_tab2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,null)
                rd_health_tab3.setCompoundDrawablesWithIntrinsicBounds(null, null, null,null)
            }
            1 -> {
                rd_health_tab1.setCompoundDrawablesWithIntrinsicBounds(null, null, null,null)
                rd_health_tab2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,mBorder)
                rd_health_tab3.setCompoundDrawablesWithIntrinsicBounds(null, null, null,null)
            }
            2 -> {
                rd_health_tab1.setCompoundDrawablesWithIntrinsicBounds(null, null, null,null)
                rd_health_tab2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,null)
                rd_health_tab3.setCompoundDrawablesWithIntrinsicBounds(null, null, null,mBorder)
            }
        }
    }

}
