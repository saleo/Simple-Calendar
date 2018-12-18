package net.euse.calendar.models

data class DayMonthly(val value: Int, val isThisMonth: Boolean, val isToday: Boolean, val code: String, val weekOfYear: Int, var dayEvents: ArrayList<Event>) {

    fun hasEvent() = dayEvents.isNotEmpty()
}
