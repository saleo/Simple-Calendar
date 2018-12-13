package com.simplemobiletools.calendar.fragments

import android.support.v4.app.ListFragment
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.RadioGroup
import android.widget.SimpleAdapter
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.extensions.config
import com.simplemobiletools.calendar.helpers.*
import com.simplemobiletools.calendar.helpers.Formatter
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import kotlinx.android.synthetic.main.fragment_about_health.*
import kotlinx.android.synthetic.main.health_list_header.view.*
import java.util.*

class HealthFragment: ListFragment(),RadioGroup.OnCheckedChangeListener {
    private lateinit var mHeader: View
    private var mOldHeader: View? = null
    private var mBorder: Drawable? =null
    var currentDayCode="19700101"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_about_health,container,false)
        view.background = ColorDrawable(activity!!.config.backgroundColor)
        return view
    }

    override fun onResume() {
        super.onResume()
        currentDayCode= Formatter.getTodayCode(activity!!)
        (activity as MainActivity).updateTopBottom(view = ABOUT_CREDIT_VIEW)

        mBorder=resources.getDrawable(R.drawable.divider_green)
        val h=mBorder!!.intrinsicHeight
        val w= mBorder!!.intrinsicWidth
        mBorder!!.setBounds(0,0,w,h)

        mHeader = View.inflate(activity, R.layout.health_list_header, null)

        setupTabs()
        setupListview()
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
                mHeader.header_health_title.text = resources.getString(R.string.str_health_headerviewtext1)
                mHeader.header_health_content?.text = resources.getString(R.string.str_health_headerviewtext1_1)
            }
            1 -> {
                sa1 = resources.getStringArray(R.array.health2)
                iLen = sa1.size / 2
                mHeader.header_health_title.text = resources.getString(R.string.str_health_headerviewtext2)
                mHeader.header_health_content.text = resources.getString(R.string.str_health_headerviewtext2_1)
            }
            2 -> {
                sa1 = resources.getStringArray(R.array.health3)
                iLen = sa1.size / 2
                mHeader.header_health_title.text = resources.getString(R.string.str_health_headerviewtext3)
                mHeader.header_health_content.text == resources.getString(R.string.str_health_headerviewtext3_1)
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
            listAdapter = SimpleAdapter(activity, lst1, R.layout.health_list_item, arrayOf(HEALTH_TITLE, HEALTH_CONTENT), intArrayOf(R.id.item_health_title, R.id.item_health_content))
        else
            listAdapter = SimpleAdapter(activity, lst1, R.layout.health_list_item2, arrayOf(HEALTH_TITLE, HEALTH_CONTENT, HEALTH_CONTENT2), intArrayOf(R.id.item_health_title, R.id.item_health_content, R.id.item_health_content2))

        listView.adapter=listAdapter

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
