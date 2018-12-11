package com.simplemobiletools.calendar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.helpers.DAY_CODE
import com.simplemobiletools.calendar.helpers.Formatter
import com.simplemobiletools.calendar.helpers.QINGXIN_VIEW
import kotlinx.android.synthetic.main.fragment_qingxin.*

class QingxinFragment:MyFragmentHolder() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_qingxin,container,false)

        currentDayCode=arguments!!.getString(DAY_CODE)

        return view
    }

    override fun onResume() {
        super.onResume()
        val dt1=Formatter.getDateTimeFromCode(currentDayCode)
        val iYear=dt1.year
        val iMonth=dt1.monthOfYear
        val sentences=resources.getStringArray(R.array.bottom_sentences)

        if (iYear==2016 || iYear ==2018){
            txt_qingxin1.text=sentences[3*(iMonth-1)]
            txt_qingxin2.text=sentences[3*(iMonth-1)+1]
            txt_qingxin3.text=sentences[3*(iMonth-1)+2]
        }else if (iYear==2017 || iYear==2019){
            txt_qingxin1.text=sentences[3*(iMonth-1)+36]
            txt_qingxin2.text=sentences[3*(iMonth-1)+37]
            txt_qingxin3.text=sentences[3*(iMonth-1)+38]
        }else{
            txt_qingxin1.text=sentences[3*(iMonth-1)]
            txt_qingxin2.text=sentences[3*(iMonth-1)+1]
            txt_qingxin3.text=sentences[3*(iMonth-1)+2]
        }        
        (activity as MainActivity).updateTopBottom(Formatter.getDateTimeFromCode(currentDayCode), QINGXIN_VIEW)
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


