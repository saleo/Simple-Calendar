package net.euse.skcal.interfaces

import net.euse.skcal.models.Event

interface WeeklyCalendar {
    fun updateWeeklyCalendar(events: ArrayList<Event>)
}
