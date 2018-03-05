package net.euse.skcal.interfaces

import android.content.Context
import net.euse.skcal.models.DayMonthly

interface MonthlyCalendar {
    fun updateMonthlyCalendar(context: Context, month: String, days: List<DayMonthly>, checkedEvents: Boolean)
}
