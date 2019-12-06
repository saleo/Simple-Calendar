package net.euse.calendar.dialogs

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import android.text.Editable
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import net.euse.calendar.R
import net.euse.calendar.activities.SimpleActivity
import com.simplemobiletools.commons.extensions.setupDialogStuff

import cn.carbs.android.gregorianlunarcalendar.library.data.ChineseCalendar
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.underlineText
import com.simplemobiletools.commons.extensions.value
import kotlinx.android.synthetic.main.dialog_customize_event.view.*
import kotlinx.android.synthetic.main.dialog_customize_lunar.*
import kotlinx.android.synthetic.main.dialog_customize_lunar.view.*
import java.util.*

class CustomizeEventDialog(val activity: SimpleActivity, val whomfor: String="", val whatfor: String="", val lunarDate: String="", internal val callback: (cb_whomfor: String, cb_whatfor: String, cb_lunarDate: String, cb_gregorianDate: String) -> Unit):
        DialogInterface.OnClickListener,AdapterView.OnItemSelectedListener {
    private var mWhomfor=whomfor
    private var mWhatfor=whatfor

    var dialog: AlertDialog

    val view = (activity.layoutInflater.inflate(R.layout.dialog_customize_event, null) as ViewGroup)

    init{
        dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok,this)
                .setNegativeButton(R.string.cancel,null)
                .create().apply {
                    activity.setupDialogStuff(view, this) {
                        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                        window!!.decorView.setPadding(0, 0, 0, 0)
                    }
                    setupDialogComponents()
                }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        val myLunar=view.tv_dialog_customize_event_when.value
        if (mWhatfor==whatfor && mWhomfor == whomfor && myLunar == lunarDate){
            activity.toast("no change,please check and save again")
            return
        }

        val myGreg=view.tv_dialog_customize_event_when_gregorian.value
        callback(mWhomfor,mWhatfor,myLunar,myGreg)
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

        view.tv_dialog_customize_event_when.apply {
            underlineText()
            text=lunarDate
            setOnClickListener {
                CustomizeLunarDialog(activity, lunarDate = this.value) { lunarDate, gregorianDate ->
                    text = lunarDate
                    view.tv_dialog_customize_event_when_gregorian.text = gregorianDate
                }
            }
        }

    }
}
