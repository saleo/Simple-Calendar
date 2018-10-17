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
import kotlinx.android.synthetic.main.fragment_qingxin.*
import kotlinx.android.synthetic.main.fragment_qingxin.view.*
import org.joda.time.DateTime
import com.xyzlf.share.library.interfaces.ShareConstant
import com.xyzlf.share.library.util.ShareUtil
import com.xyzlf.share.library.bean.ShareEntity



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
        setupQingxin(Formatter.getDateTimeFromCode(mDayCode))
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

        img_forward1.setOnClickListener {
            val testBean = ShareEntity("我是标题", "我是内容，描述内容。")
            testBean.url = "https://www.baidu.com" //分享链接
            testBean.imgUrl = "https://www.baidu.com/img/bd_logo1.png"
            ShareUtil.startShare(activity, ShareConstant.SHARE_CHANNEL_WEIXIN_FRIEND, testBean, ShareConstant.REQUEST_CODE)
        }
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


