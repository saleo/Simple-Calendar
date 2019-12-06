package net.euse.calendar.fragments

import androidx.fragment.app.Fragment

abstract class MyFragmentHolder : androidx.fragment.app.Fragment() {
    var currentDayCode="19700101"

    abstract fun goToToday()

    abstract fun refreshEvents()

    abstract fun shouldGoToTodayBeVisible(): Boolean

    abstract fun updateActionBarTitle()

    abstract fun getNewEventDayCode(): String
}
