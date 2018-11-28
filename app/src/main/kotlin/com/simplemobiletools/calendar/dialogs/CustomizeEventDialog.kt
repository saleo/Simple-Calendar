package com.simplemobiletools.calendar.dialogs

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.SimpleActivity
import com.simplemobiletools.commons.extensions.setupDialogStuff

import cn.carbs.android.gregorianlunarcalendar.library.data.ChineseCalendar
import com.simplemobiletools.commons.extensions.value
import kotlinx.android.synthetic.main.dialog_customize_event.view.*
import kotlinx.android.synthetic.main.dialog_customize_lunar.*
import kotlinx.android.synthetic.main.dialog_customize_lunar.view.*
import java.util.*

class CustomizeEventDialog(val activity: SimpleActivity, val whomfor: String="", val whatfor: String="", val lunarDate: String="", internal val callback: (cb_whomfor: String, cb_whatfor: String, cb_lunarDate: String, cb_gregorianDate: String) -> Unit):
        DialogInterface.OnClickListener,AdapterView.OnItemSelectedListener {
    var mWhomfor=whomfor
    var mWhatfor=whatfor
    var dialog: AlertDialog

    val view = (activity.layoutInflater.inflate(R.layout.dialog_customize_event, null) as ViewGroup).apply {
        val customizedCalendar = Calendar.getInstance()
        if (lunarDate!="")
            customizedCalendar.set(lunarDate.substring(0, 3).toInt(), lunarDate.substring(4, 5).toInt(), lunarDate.substring(6, 7).toInt())
        //no else needed, since getInstance() created a calendar based current time in the defaulst zone
        calendar_view.init(customizedCalendar, false)
        setupDialogComponents()
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
        callback(mWhomfor,mWhatfor,lunarDate,gregorianDate)
        dialog!!.dismiss()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent){
            view!!.acs_dialog_customize_event_whomfor -> {mWhomfor= parent?.getItemAtPosition(position).toString()}
            view!!.acs_dialog_customize_event_whatfor -> {mWhatfor= parent?.getItemAtPosition(position).toString()}
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun setupDialogComponents(){
        val whomfor_array = activity.resources.getStringArray(R.array.whomfor)
        val whomfor_adapter = ArrayAdapter<CharSequence>(activity, android.R.layout.simple_spinner_item, whomfor_array)
        whomfor_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.acs_dialog_customize_event_whomfor.apply {
            adapter = whomfor_adapter
            when (whomfor){
                whomfor_array[0]-> setSelection(0)
                whomfor_array[1]-> setSelection(1)
            }

            onItemSelectedListener = this@CustomizeEventDialog
        }

        val whatfor_array = activity.resources.getStringArray(R.array.whatfor)
        val whatfor_adapter = ArrayAdapter<CharSequence>(activity, android.R.layout.simple_spinner_item, whatfor_array)
        whatfor_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.acs_dialog_customize_event_whatfor.apply {
            adapter=whatfor_adapter
            when (whatfor){
                whatfor_array[0] -> setSelection(0)
                whatfor_array[1] -> setSelection(1)
            }
            onItemSelectedListener = this@CustomizeEventDialog
        }

        view.tv_dialog_customize_event_when.text=lunarDate
        view.tv_dialog_customize_event_when.setOnClickListener {
            CustomizeLunarDialog(activity, view.tv_dialog_customize_event_when.value) { lunarDate, gregorianDate ->
                view.tv_dialog_customize_event_when.text = lunarDate.substring(4,8)
                view.tv_dialog_customize_event_when_gregorian.text = gregorianDate
            }
        }        
    }
}
