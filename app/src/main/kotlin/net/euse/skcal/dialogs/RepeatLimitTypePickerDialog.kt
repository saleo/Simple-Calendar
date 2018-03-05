package net.euse.skcal.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.support.v7.app.AlertDialog
import android.view.View
import net.euse.skcal.extensions.config
import net.euse.skcal.extensions.getNowSeconds
import net.euse.skcal.extensions.seconds
import net.euse.skcal.helpers.Formatter
import com.simplemobiletools.commons.extensions.getDialogTheme
import com.simplemobiletools.commons.extensions.isLollipopPlus
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.value
import kotlinx.android.synthetic.main.dialog_repeat_limit_type_picker.view.*
import org.joda.time.DateTime
import java.util.*

class RepeatLimitTypePickerDialog(val activity: Activity, var repeatLimit: Int, val startTS: Int, val callback: (repeatLimit: Int) -> Unit) {
    lateinit var dialog: AlertDialog
    var view: View

    init {
        view = activity.layoutInflater.inflate(net.euse.skcal.R.layout.dialog_repeat_limit_type_picker, null).apply {
            repeat_type_date.setOnClickListener { showRepetitionLimitDialog() }
            repeat_type_forever.setOnClickListener { callback(0); dialog.dismiss() }
            repeat_type_count.setOnClickListener { dialog_radio_view.check(net.euse.skcal.R.id.repeat_type_x_times) }
        }

        view.dialog_radio_view.check(getCheckedItem())

        if (repeatLimit in 1..startTS)
            repeatLimit = startTS

        updateRepeatLimitText()

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(net.euse.skcal.R.string.ok, { dialogInterface, i -> confirmRepetition() })
                .setNegativeButton(net.euse.skcal.R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this) {
                activity.currentFocus?.clearFocus()
            }
        }
    }

    private fun getCheckedItem() = when {
        repeatLimit > 0 -> net.euse.skcal.R.id.repeat_type_till_date
        repeatLimit < 0 -> {
            view.repeat_type_count.setText((-repeatLimit).toString())
            net.euse.skcal.R.id.repeat_type_x_times
        }
        else -> net.euse.skcal.R.id.repeat_type_forever
    }

    private fun updateRepeatLimitText() {
        if (repeatLimit <= 0)
            repeatLimit = activity.getNowSeconds()

        val repeatLimitDateTime = Formatter.getDateTimeFromTS(repeatLimit)
        view.repeat_type_date.text = Formatter.getFullDate(activity, repeatLimitDateTime)
    }

    private fun confirmRepetition() {
        when (view.dialog_radio_view.checkedRadioButtonId) {
            net.euse.skcal.R.id.repeat_type_till_date -> callback(repeatLimit)
            net.euse.skcal.R.id.repeat_type_forever -> callback(0)
            else -> {
                var count = view.repeat_type_count.value
                if (count.isEmpty())
                    count = "0"
                else
                    count = "-$count"
                callback(count.toInt())
            }
        }
        dialog.dismiss()
    }

    @SuppressLint("NewApi")
    private fun showRepetitionLimitDialog() {
        val repeatLimitDateTime = Formatter.getDateTimeFromTS(if (repeatLimit != 0) repeatLimit else activity.getNowSeconds())
        val datepicker = DatePickerDialog(activity, activity.getDialogTheme(), repetitionLimitDateSetListener, repeatLimitDateTime.year,
                repeatLimitDateTime.monthOfYear - 1, repeatLimitDateTime.dayOfMonth)

        if (activity.isLollipopPlus()) {
            datepicker.datePicker.firstDayOfWeek = if (activity.config.isSundayFirst) Calendar.SUNDAY else Calendar.MONDAY
        }

        datepicker.show()
    }

    private val repetitionLimitDateSetListener = DatePickerDialog.OnDateSetListener { v, year, monthOfYear, dayOfMonth ->
        val repeatLimitDateTime = DateTime().withDate(year, monthOfYear + 1, dayOfMonth).withTime(23, 59, 59, 0)
        repeatLimit = if (repeatLimitDateTime.seconds() < startTS) {
            0
        } else {
            repeatLimitDateTime.seconds()
        }
        callback(repeatLimit)
        dialog.dismiss()
    }
}
