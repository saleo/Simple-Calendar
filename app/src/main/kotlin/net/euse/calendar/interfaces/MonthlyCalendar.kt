package net.euse.calendar.interfaces

import android.content.Context
import net.euse.calendar.models.DayMonthly

interface MonthlyCalendar {
    fun updateMonthlyCalendar(context: Context, month: String, days: List<DayMonthly>, checkedEvents: Boolean)
}
