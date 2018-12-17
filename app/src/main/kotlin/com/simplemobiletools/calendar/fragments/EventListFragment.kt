package com.simplemobiletools.calendar.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.EventActivity
import com.simplemobiletools.calendar.activities.SimpleActivity
import com.simplemobiletools.calendar.adapters.EventListAdapter
import com.simplemobiletools.calendar.extensions.*
import com.simplemobiletools.calendar.helpers.*
import com.simplemobiletools.calendar.helpers.Formatter
import com.simplemobiletools.calendar.models.Event
import com.simplemobiletools.calendar.models.ListEvent
import com.simplemobiletools.commons.extensions.beGoneIf
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import kotlinx.android.synthetic.main.fragment_event_list.view.*
import java.util.*

class EventListFragment : Fragment(), RefreshRecyclerViewListener {
    private var mEvents: List<Event> = ArrayList()
    private var prevEventsHash = 0
    private var use24HourFormat = false
    lateinit var mView: View
    private var mDayCode=""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_event_list, container, false)

        use24HourFormat = context!!.config.use24hourFormat
        mDayCode = arguments!!.getString(DAY_CODE)
        return mView
    }

    override fun onResume() {
        super.onResume()
        val placeholderText = String.format(getString(R.string.string_placeholder), "${getString(R.string.no_upcoming_events_in_eventlist)}\n")
        mView.calendar_empty_list_placeholder.text = placeholderText

        updateCalendar()
        val use24Hour = context!!.config.use24hourFormat
        if (use24Hour != use24HourFormat) {
            use24HourFormat = use24Hour
            (mView.calendar_events_list.adapter as? EventListAdapter)?.toggle24HourFormat(use24HourFormat)
        }
    }

    override fun onPause() {
        super.onPause()
        use24HourFormat = context!!.config.use24hourFormat
    }

    fun updateCalendar() {
        val targetDate=Formatter.getDateTimeFromCode(mDayCode)
        val fromTS = targetDate.dayOfMonth().withMinimumValue().seconds()
        val toTS= targetDate.dayOfMonth().withMaximumValue().seconds()

        context!!.dbHelper.getEvents(fromTS, toTS) {
            receivedEvents(it)
        }
    }

    private fun receivedEvents(events: MutableList<Event>) {
        if (context == null || activity == null) {
            return
        }

        val filtered = context!!.getFilteredEvents(events)
        val hash = filtered.hashCode()
        if (prevEventsHash == hash) {
            return
        }

        prevEventsHash = hash
        mEvents = filtered
        val listItems = context!!.getEventListItems(mEvents)

        val eventsAdapter = EventListAdapter(activity as SimpleActivity, listItems, true, this, mView.calendar_events_list) {
//            if (it is ListEvent) {
//                editEvent(it)
//            }
        }

        activity?.runOnUiThread {
            mView.calendar_events_list.adapter = eventsAdapter
            checkPlaceholderVisibility()
        }
    }

    private fun checkPlaceholderVisibility() {
        mView.calendar_empty_list_placeholder.beVisibleIf(mEvents.isEmpty())
        mView.calendar_events_list.beGoneIf(mEvents.isEmpty())
        if (activity != null)
            mView.calendar_empty_list_placeholder.setTextColor(activity!!.config.textColor)
    }

    private fun editEvent(event: ListEvent) {
        Intent(context, EventActivity::class.java).apply {
            putExtra(EVENT_ID, event.id)
            putExtra(EVENT_OCCURRENCE_TS, event.startTS)
            startActivity(this)
        }
    }


    override fun refreshItems() {
        updateCalendar()
    }

}
