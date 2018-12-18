package net.euse.calendar.helpers

import android.content.Context
import net.euse.calendar.extensions.dbHelper
import net.euse.calendar.interfaces.WeeklyCalendar
import net.euse.calendar.models.Event
import java.util.*

class WeeklyCalendarImpl(val mCallback: WeeklyCalendar, val mContext: Context) {
    var mEvents = ArrayList<Event>()

    fun updateWeeklyCalendar(weekStartTS: Int) {
        val startTS = weekStartTS
        val endTS = startTS + WEEK_SECONDS
        mContext.dbHelper.getEvents(startTS, endTS) {
            mEvents = it as ArrayList<Event>
            mCallback.updateWeeklyCalendar(it)
        }
    }
}
