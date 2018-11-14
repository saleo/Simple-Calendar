package com.simplemobiletools.calendar.fragments

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.annotation.MainThread
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.helpers.DAY_CODE
import com.simplemobiletools.calendar.helpers.Formatter
import kotlinx.android.synthetic.main.bottom_contact_copyright.*
import kotlinx.android.synthetic.main.fragment_qingxin.*
import kotlinx.android.synthetic.main.fragment_qingxin.view.*
import org.joda.time.DateTime
import java.util.*

class QingxinFragment:MyFragmentHolder() {

    private var mDayCode=""

    lateinit var mRes: Resources
    lateinit var mHolder: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_qingxin,container,false)
        mRes=resources
        mHolder=view.rl_qingxin_holder

        mDayCode=arguments!!.getString(DAY_CODE)

        return view
    }

    override fun onResume() {
        super.onResume()
        setupCopyright()
        setupQingxin(Formatter.getDateTimeFromCode(mDayCode))

    }

    private fun setupCopyright() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright), year)
    }

    private fun setupQingxin(time: DateTime){
        val bottomeSentences=mRes.getStringArray(R.array.bottom_sentences)
        val iYear=time.year
        val iMonth=time.monthOfYear
        if (iYear == 2016 || iYear==2018) {
            txt_qingxin1!!.text=bottomeSentences[iMonth+3*(iMonth-1)]
            txt_qingxin2!!.text=bottomeSentences[iMonth+3*(iMonth-1)+1]
            txt_qingxin3!!.text=bottomeSentences[iMonth+3*(iMonth-1)+2]
        } else if (iYear == 2017 || iYear==2019) {
            txt_qingxin1!!.text=bottomeSentences[iMonth+36+3*(iMonth-1)]
            txt_qingxin2!!.text=bottomeSentences[iMonth+36+3*(iMonth-1)+1]
            txt_qingxin3!!.text=bottomeSentences[iMonth+36+3*(iMonth-1)+2]
        } else {
            txt_qingxin1!!.text=bottomeSentences[0]
            txt_qingxin2!!.text=bottomeSentences[1]
            txt_qingxin3!!.text=bottomeSentences[2]
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
        (activity as MainActivity).supportActionBar?.title = getString(R.string.app_launcher_name)
    }


    override fun getNewEventDayCode(): String {
        return ""
    }
}


