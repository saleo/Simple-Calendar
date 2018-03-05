package net.euse.skcal.dialogs

import android.support.v7.app.AlertDialog
import android.support.v7.widget.SwitchCompat
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.RelativeLayout
import net.euse.skcal.extensions.config
import net.euse.skcal.helpers.CalDAVHandler
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.calendar_item_account.view.*
import kotlinx.android.synthetic.main.calendar_item_calendar.view.*
import kotlinx.android.synthetic.main.dialog_select_calendars.view.*

class SelectCalendarsDialog(val activity: net.euse.skcal.activities.SimpleActivity, val callback: () -> Unit) {
    var prevAccount = ""
    var dialog: AlertDialog
    var view = (activity.layoutInflater.inflate(net.euse.skcal.R.layout.dialog_select_calendars, null) as ViewGroup)

    init {
        val ids = activity.config.getSyncedCalendarIdsAsList()
        val calendars = CalDAVHandler(activity.applicationContext).getCalDAVCalendars(activity)
        val sorted = calendars.sortedWith(compareBy({ it.accountName }, { it.displayName }))
        sorted.forEach {
            if (prevAccount != it.accountName) {
                prevAccount = it.accountName
                addCalendarItem(false, it.accountName)
            }

            addCalendarItem(true, it.displayName, it.id, ids.contains(it.id.toString()))
        }

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(net.euse.skcal.R.string.ok, { dialogInterface, i -> confirmSelection() })
                .setNegativeButton(net.euse.skcal.R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this, net.euse.skcal.R.string.select_caldav_calendars)
        }
    }

    private fun addCalendarItem(isEvent: Boolean, text: String, tag: Int = 0, shouldCheck: Boolean = false) {
        val calendarItem = activity.layoutInflater.inflate(if (isEvent) net.euse.skcal.R.layout.calendar_item_calendar else net.euse.skcal.R.layout.calendar_item_account,
                view.dialog_select_calendars_holder, false)

        if (isEvent) {
            calendarItem.calendar_item_calendar_switch.apply {
                this.tag = tag
                this.text = text
                isChecked = shouldCheck
                calendarItem.setOnClickListener {
                    toggle()
                }
            }
        } else {
            calendarItem.calendar_item_account.text = text
        }

        view.dialog_select_calendars_holder.addView(calendarItem)
    }

    private fun confirmSelection() {
        val calendarIDs = ArrayList<Int>()
        val childCnt = view.dialog_select_calendars_holder.childCount
        for (i in 0..childCnt) {
            val child = view.dialog_select_calendars_holder.getChildAt(i)
            if (child is RelativeLayout) {
                val check = child.getChildAt(0)
                if (check is SwitchCompat && check.isChecked) {
                    calendarIDs.add(check.tag as Int)
                }
            }
        }

        activity.config.caldavSyncedCalendarIDs = TextUtils.join(",", calendarIDs)
        callback()
    }
}
