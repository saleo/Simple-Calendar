package com.simplemobiletools.calendar.dialogs

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.view.WindowManager
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.SimpleActivity
import com.simplemobiletools.commons.extensions.setupDialogStuff

import cn.carbs.android.gregorianlunarcalendar.library.data.ChineseCalendar
import cn.carbs.android.gregorianlunarcalendar.library.view.GregorianLunarCalendarView
import com.simplemobiletools.calendar.helpers.YEAR
import kotlinx.android.synthetic.main.dialog_customize_lunar.*
import kotlinx.android.synthetic.main.dialog_customize_lunar.view.*
import java.util.*

class CustomizeLunarDialog(val activity: SimpleActivity, val lunarDate: String="", val callback: (lunarDate: String, gregorianDate: String)-> Unit):DialogInterface.OnClickListener {
    var dialog: AlertDialog
    var customizeCalendar=ChineseCalendar()
    val view = (activity.layoutInflater.inflate(R.layout.dialog_customize_lunar, null) as ViewGroup).apply {
        var lunarYear=0;var lunarMonth=0;var lunarDay=0
        if (lunarDate!="") {
            lunarYear = lunarDate.substring(0, 4).toInt();lunarMonth = lunarDate.substring(4, 6).toInt();lunarDay = lunarDate.substring(6, 8).toInt();
            customizeCalendar = ChineseCalendar(true, lunarYear, lunarMonth, lunarDay)
        }else
            customizeCalendar=ChineseCalendar()

        glc_calendar_view.init(customizeCalendar, false)
        //no date-set needed, since getInstance() created a calendar based current time in the defaulst zone
    }

    init{
        dialog = android.support.v7.app.AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok,this)
                .setNegativeButton(R.string.cancel,null)
                .create().apply {
                    activity.setupDialogStuff(view, this) {
                        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                        window!!.decorView.setPadding(0, 0, 0, 0)
                        glc_calendar_view!!.toLunarMode()
                    }
                }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        val calendarData = view.glc_calendar_view.calendarData
        val calendar = calendarData.calendar
        var sMonth="";var sDay=""
        var iMonth=calendar.get(ChineseCalendar.CHINESE_MONTH)
        var iDay=calendar.get(ChineseCalendar.CHINESE_DATE)
        if (iMonth<10) sMonth="0$iMonth" else sMonth=iMonth.toString()
        if (iDay<10) sDay="0$iDay" else sDay=iDay.toString()
        val lunarDate= calendar.get(ChineseCalendar.CHINESE_YEAR).toString()+sMonth+sDay
        iMonth=calendar.get(Calendar.MONTH)
        iDay=calendar.get(Calendar.DAY_OF_MONTH)
        if (iMonth<10) sMonth="0$iMonth" else sMonth=iMonth.toString()
        if (iDay<10) sDay="0$iDay" else sDay=iDay.toString()
        val gregorianDate=calendar.get(Calendar.YEAR).toString()+sMonth+sDay
        callback(lunarDate,gregorianDate)
        dialog!!.dismiss()
    }

}
