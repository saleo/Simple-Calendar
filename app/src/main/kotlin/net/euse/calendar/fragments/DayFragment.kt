package net.euse.calendar.fragments

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.RelativeLayout
import net.euse.calendar.R
import net.euse.calendar.activities.EventActivity
import net.euse.calendar.activities.SimpleActivity
import net.euse.calendar.adapters.DayEventsAdapter
import net.euse.calendar.extensions.config
import net.euse.calendar.extensions.dbHelper
import net.euse.calendar.extensions.getFilteredEvents
import net.euse.calendar.helpers.*
import net.euse.calendar.helpers.Formatter
import net.euse.calendar.interfaces.NavigationListener
import net.euse.calendar.models.Event
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.fragment_day.*
import kotlinx.android.synthetic.main.fragment_day.view.*
import org.joda.time.DateTime
import java.util.*

class DayFragment : androidx.fragment.app.Fragment() {
    var mListener: NavigationListener? = null
    private var mTextColor = 0
    private var mDayCode = ""
    private var lastHash = 0

    lateinit var mRes: Resources
    lateinit var mHolder: RelativeLayout
    lateinit var mConfig: Config

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_day, container, false)
        mRes = resources

        mDayCode = arguments!!.getString(DAY_CODE)
        return view
    }

    override fun onResume() {
        super.onResume()
        val placeholderText = String.format(getString(R.string.string_placeholder), "${getString(R.string.no_upcoming_events_in_dayEvents)}\n")
        tv_day_events_placeholder.text = placeholderText
        mHolder = rl_day_holder
        setupButtons()
        updateCalendar()
    }

    private fun setupButtons() {
        mConfig =context!!.config
        mTextColor = context!!.config.textColor

//        mHolder.top_left_arrow.apply {
//            applyColorFilter(mTextColor)
//            background = null
//            setOnClickListener {
//                mListener?.goLeft()
//            }
//        }
//
//        mHolder.top_right_arrow.apply {
//            applyColorFilter(mTextColor)
//            background = null
//            setOnClickListener {
//                mListener?.goRight()
//            }
//        }

//        val day = Formatter.getDayTitle(context!!, mDayCode)
//        mHolder.top_value.apply {
//            text = day
//            setOnClickListener { pickDay() }
//            setTextColor(context.config.textColor)
//        }
    }

    private fun pickDay() {
        activity!!.setTheme(context!!.getDialogTheme())
        val view = layoutInflater.inflate(R.layout.date_picker, null)
        val datePicker = view.findViewById<DatePicker>(R.id.date_picker)

        val dateTime = Formatter.getDateTimeFromCode(mDayCode)
        datePicker.init(dateTime.year, dateTime.monthOfYear - 1, dateTime.dayOfMonth, null)

        AlertDialog.Builder(context!!)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok) { dialog, which -> positivePressed(dateTime, datePicker) }
                .create().apply {
            activity?.setupDialogStuff(view, this)
        }
    }

    private fun positivePressed(dateTime: DateTime, datePicker: DatePicker) {
        val month = datePicker.month + 1
        val year = datePicker.year
        val day = datePicker.dayOfMonth
        val newDateTime = dateTime.withDate(year, month, day)
        mListener?.goToDateTime(newDateTime)
    }

    fun updateCalendar() {
        val startTS = Formatter.getDayStartTS(mDayCode)
        val endTS = Formatter.getDayEndTS(mDayCode)
        context!!.dbHelper.getEvents(startTS, endTS) {
            receivedEvents(it)
        }
    }

    private fun receivedEvents(events: List<Event>) {
        val filtered = context?.getFilteredEvents(events) ?: ArrayList()
        val newHash = filtered.hashCode()
        if (newHash == lastHash || !isAdded) {
            return
        }
        lastHash = newHash

        val replaceDescription = context!!.config.replaceDescription
        val sorted = ArrayList<Event>(filtered.sortedWith(compareBy({ it.startTS }, { it.endTS }, { it.title }, {
            if (replaceDescription) it.location else it.description
        })))

        activity?.runOnUiThread {
            updateEvents(sorted)
            tv_day_events_placeholder.beVisibleIf(events.isEmpty())
            rv_day_events.beGoneIf(events.isEmpty())
        }
    }

    private fun updateEvents(events: ArrayList<Event>) {
        if (activity == null)
            return

        DayEventsAdapter(activity as SimpleActivity, events, mHolder.rv_day_events) {
            editEvent(it as Event)
        }.apply {
            setupDragListener(false)
            addVerticalDividers(true)
            mHolder.rv_day_events.adapter=this
        }
    }

    private fun editEvent(event: Event) {
        Intent(context, EventActivity::class.java).apply {
            putExtra(EVENT_ID, event.id)
            putExtra(EVENT_OCCURRENCE_TS, event.startTS)
            startActivity(this)
        }
    }

}
