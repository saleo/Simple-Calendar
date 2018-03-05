package net.euse.skcal.helpers

import android.content.Context
import net.euse.skcal.extensions.dbHelper
import net.euse.skcal.interfaces.WeeklyCalendar
import net.euse.skcal.models.Event
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
