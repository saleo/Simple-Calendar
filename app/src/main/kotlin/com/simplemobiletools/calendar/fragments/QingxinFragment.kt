package com.simplemobiletools.calendar.fragments

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.helpers.DAY_CODE
import com.simplemobiletools.calendar.helpers.Formatter
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import kotlinx.android.synthetic.main.fragment_qingxin.*
import kotlinx.android.synthetic.main.fragment_qingxin.view.*
import java.util.*

class QingxinFragment:MyFragmentHolder() {

    private var mDayCode=""

    lateinit var mRes: Resources
    lateinit var mHolder: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_qingxin,container,false)
        mRes=resources
        mHolder=view.ll_qingxin_holder

        mDayCode=arguments!!.getString(DAY_CODE)

        return view
    }

    override fun onResume() {
        super.onResume()
        setupCopyright()
        (activity as MainActivity).updateContentBasedMonth(Formatter.getDateTimeFromCode(mDayCode),ll_qingxin_holder)

    }

    private fun setupCopyright() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright), year)
    }

//        img_forward1.setOnClickListener {
//            Intent().apply {
//                action = Intent.ACTION_SEND
//                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
//                putExtra(Intent.EXTRA_TEXT, txt_qingxin1.text)
//                type = "text/plain"
//                activity!!.startActivity(Intent.createChooser(this, getString(R.string.invite_via)))
//            }
//        }

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


