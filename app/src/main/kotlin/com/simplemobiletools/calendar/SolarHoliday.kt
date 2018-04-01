package com.simplemobiletools.calendar

import android.content.Context

object SolarHoliday {
    fun getSolarHoliday(currentMonth: Int, currentDayForMonth: Int, context: Context): String {
        val num_date = String.format("%02d", currentMonth + 1) + "" + String.format("%02d", currentDayForMonth)
        val solarHolidayArray = context.resources.getStringArray(R.array.solar_holiday)
        for (i in solarHolidayArray.indices) {
            val solarHolidayDateStr = solarHolidayArray[i].split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (solarHolidayDateStr[0] == num_date) {
                return solarHolidayDateStr[1]
            }
        }
        return ""
    }
}
