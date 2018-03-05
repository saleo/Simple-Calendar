package net.euse.skcal.extensions

import net.euse.skcal.helpers.Formatter
import net.euse.skcal.helpers.MONTH
import net.euse.skcal.helpers.WEEK
import net.euse.skcal.models.Event

fun Int.isTsOnProperDay(event: Event): Boolean {
    val dateTime = Formatter.getDateTimeFromTS(this)
    val power = Math.pow(2.0, (dateTime.dayOfWeek - 1).toDouble()).toInt()
    return event.repeatRule and power != 0
}

fun Int.isXWeeklyRepetition() = this != 0 && this % WEEK == 0

fun Int.isXMonthlyRepetition() = this != 0 && this % MONTH == 0
