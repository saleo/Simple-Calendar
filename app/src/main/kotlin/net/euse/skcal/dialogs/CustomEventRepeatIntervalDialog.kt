package net.euse.skcal.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.view.WindowManager
import net.euse.skcal.helpers.DAY
import net.euse.skcal.helpers.MONTH
import net.euse.skcal.helpers.WEEK
import net.euse.skcal.helpers.YEAR
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.value
import kotlinx.android.synthetic.main.dialog_custom_event_repeat_interval.view.*

class CustomEventRepeatIntervalDialog(val activity: Activity, val callback: (seconds: Int) -> Unit) {
    var dialog: AlertDialog
    var view = activity.layoutInflater.inflate(net.euse.skcal.R.layout.dialog_custom_event_repeat_interval, null) as ViewGroup

    init {
        view.dialog_radio_view.check(net.euse.skcal.R.id.dialog_radio_days)

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(net.euse.skcal.R.string.ok, { dialogInterface, i -> confirmRepeatInterval() })
                .setNegativeButton(net.euse.skcal.R.string.cancel, null)
                .create().apply {
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            activity.setupDialogStuff(view, this)
        }
    }

    private fun confirmRepeatInterval() {
        val value = view.dialog_custom_repeat_interval_value.value
        val multiplier = getMultiplier(view.dialog_radio_view.checkedRadioButtonId)
        val days = Integer.valueOf(if (value.isEmpty()) "0" else value)
        callback(days * multiplier)
        activity.hideKeyboard()
        dialog.dismiss()
    }

    private fun getMultiplier(id: Int) = when (id) {
        net.euse.skcal.R.id.dialog_radio_weeks -> WEEK
        net.euse.skcal.R.id.dialog_radio_months -> MONTH
        net.euse.skcal.R.id.dialog_radio_years -> YEAR
        else -> DAY
    }
}
