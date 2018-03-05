package net.euse.skcal.fragments

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.euse.skcal.helpers.EVENT_ID
import net.euse.skcal.helpers.EVENT_OCCURRENCE_TS
import net.euse.skcal.helpers.Formatter
import net.euse.skcal.models.Event
import net.euse.skcal.models.ListEvent
import com.simplemobiletools.commons.extensions.beGoneIf
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import kotlinx.android.synthetic.main.fragment_event_list.view.*
import net.euse.skcal.extensions.*
import org.joda.time.DateTime
import java.util.*

class EventListFragment : MyFragmentHolder(), RefreshRecyclerViewListener {
    private var mEvents: List<Event> = ArrayList()
    private var prevEventsHash = 0
    private var use24HourFormat = false
    lateinit var mView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(net.euse.skcal.R.layout.fragment_event_list, container, false)
        val placeholderText = String.format(getString(net.euse.skcal.R.string.two_string_placeholder), "${getString(net.euse.skcal.R.string.no_upcoming_events)}\n", getString(net.euse.skcal.R.string.add_some_events))
        mView.calendar_empty_list_placeholder.text = placeholderText
        mView.background = ColorDrawable(context!!.config.backgroundColor)
        mView.calendar_events_list_holder?.id = (System.currentTimeMillis() % 100000).toInt()
        use24HourFormat = context!!.config.use24hourFormat
        updateActionBarTitle()
        return mView
    }

    override fun onResume() {
        super.onResume()
        checkEvents()
        val use24Hour = context!!.config.use24hourFormat
        if (use24Hour != use24HourFormat) {
            use24HourFormat = use24Hour
            (mView.calendar_events_list.adapter as? net.euse.skcal.adapters.EventListAdapter)?.toggle24HourFormat(use24HourFormat)
        }
    }

    override fun onPause() {
        super.onPause()
        use24HourFormat = context!!.config.use24hourFormat
    }

    private fun checkEvents() {
        val fromTS = DateTime().seconds() - context!!.config.displayPastEvents * 60
        val toTS = DateTime().plusYears(1).seconds()
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

        val eventsAdapter = net.euse.skcal.adapters.EventListAdapter(activity as net.euse.skcal.activities.SimpleActivity, listItems, true, this, mView.calendar_events_list) {
            if (it is ListEvent) {
                editEvent(it)
            }
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
        Intent(context, net.euse.skcal.activities.EventActivity::class.java).apply {
            putExtra(EVENT_ID, event.id)
            putExtra(EVENT_OCCURRENCE_TS, event.startTS)
            startActivity(this)
        }
    }

    override fun refreshItems() {
        checkEvents()
    }

    override fun goToToday() {
    }

    override fun refreshEvents() {
        checkEvents()
    }

    override fun shouldGoToTodayBeVisible() = false

    override fun updateActionBarTitle() {
        (activity as net.euse.skcal.activities.MainActivity).supportActionBar?.title = getString(net.euse.skcal.R.string.app_launcher_name)
    }

    override fun getNewEventDayCode() = Formatter.getTodayCode(context!!)
}
