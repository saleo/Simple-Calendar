package com.simplemobiletools.calendar.dialogs

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.view.WindowManager
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.SimpleActivity
import com.simplemobiletools.commons.extensions.setupDialogStuff

import cn.carbs.android.gregorianlunarcalendar.library.data.ChineseCalendar
import kotlinx.android.synthetic.main.dialog_customize_lunar.*
import kotlinx.android.synthetic.main.dialog_customize_lunar.view.*
import java.util.*

class CustomizeLunarDialog(val activity: SimpleActivity, val lunarDate: String="", val callback: (lunarDate: String, gregorianDate: String)-> Unit):DialogInterface.OnClickListener {
    var dialog: AlertDialog

    val view = (activity.layoutInflater.inflate(R.layout.dialog_customize_lunar, null) as ViewGroup).apply {
        val customizedCalendar = Calendar.getInstance()
        if (lunarDate!="")
            customizedCalendar.set(lunarDate.substring(0, 4).toInt(), lunarDate.substring(4, 6).toInt(), lunarDate.substring(6, 8).toInt())
        //no else needed, since getInstance() created a calendar based current time in the defaulst zone
        calendar_view.init(customizedCalendar, false)
    }

    init{
        dialog = android.support.v7.app.AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok,this)
                .setNegativeButton(R.string.cancel,null)
                .create().apply {
                    activity.setupDialogStuff(view, this) {
                        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                        window!!.decorView.setPadding(0, 0, 0, 0)
                        calendar_view!!.toLunarMode()
                    }
                }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        val calendarData = view.calendar_view.calendarData
        val calendar = calendarData.calendar
        val lunarDate= calendar.get(ChineseCalendar.CHINESE_YEAR).toString()+calendar.get(ChineseCalendar.CHINESE_MONTH).toString()+calendar.get(ChineseCalendar.CHINESE_DATE).toString()
        val gregorianDate=calendar.get(Calendar.YEAR).toString()+calendar.get(Calendar.MONTH).toString()+calendar.get(Calendar.DAY_OF_MONTH).toString()
        callback(lunarDate,gregorianDate)
        dialog!!.dismiss()
    }

}
