package net.euse.calendar.fragments

import android.support.v4.app.Fragment

abstract class MyFragmentHolder : Fragment() {
    var currentDayCode="19700101"

    abstract fun goToToday()

    abstract fun refreshEvents()

    abstract fun shouldGoToTodayBeVisible(): Boolean

    abstract fun updateActionBarTitle()

    abstract fun getNewEventDayCode(): String
}
