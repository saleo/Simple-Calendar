package com.simplemobiletools.calendar.fragments

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.R.string.days_raw
import com.simplemobiletools.calendar.activities.EventActivity
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.activities.SimpleActivity
import com.simplemobiletools.calendar.adapters.DayEventsAdapter
import com.simplemobiletools.calendar.extensions.config
import com.simplemobiletools.calendar.extensions.dbHelper
import com.simplemobiletools.calendar.extensions.getFilteredEvents
import com.simplemobiletools.calendar.helpers.*
import com.simplemobiletools.calendar.helpers.Formatter
import com.simplemobiletools.calendar.interfaces.NavigationListener
import com.simplemobiletools.calendar.models.Event
import com.simplemobiletools.commons.extensions.getDialogTheme
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.bottom_sentense.*
import kotlinx.android.synthetic.main.fragment_day.view.*
import kotlinx.android.synthetic.main.top_navigation.view.*
import org.joda.time.DateTime
import java.util.*

class DayFragment : Fragment() {
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
        mHolder = view.day_holder

        mDayCode = arguments!!.getString(DAY_CODE)
        setupButtons()
        return view
    }

    override fun onResume() {
        super.onResume()
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

        updateMonthPlusDay(Formatter.getDateTimeFromCode(mDayCode))

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
        }
    }

    private fun updateEvents(events: ArrayList<Event>) {
        if (activity == null)
            return

        DayEventsAdapter(activity as SimpleActivity, events, mHolder.day_events) {
            editEvent(it as Event)
        }.apply {
            setupDragListener(false)
            addVerticalDividers(true)
            mHolder.day_events.adapter=this
        }
    }

    private fun editEvent(event: Event) {
        Intent(context, EventActivity::class.java).apply {
            putExtra(EVENT_ID, event.id)
            putExtra(EVENT_OCCURRENCE_TS, event.startTS)
            startActivity(this)
        }
    }
    private fun updateMonthPlusDay(time: DateTime)
    {


        //        CharSequence oldMonth = mMonthName.getText();
        //        mMonthName.setText(Utils.formatMonthYear(mContext, time));
        //        mMonthName.invalidate();
        //        if (!TextUtils.equals(oldMonth, mMonthName.getText())) {
        //            mMonthName.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        //        }
        val intYear = time.year
        val mCurrentMonthDisplayed = time.monthOfYear
        val mCurrentDay = mDayCode.substring(6,8)

        var dayNumber:TextView
        val mBottomSentences: Array<String>

        bottom_sentense0.setTextSize(mConfig.getFontSize()*1.01.toFloat())
        bottom_sentense1.setTextSize(mConfig.getFontSize()*1.01.toFloat())
        bottom_sentense2.setTextSize(mConfig.getFontSize()*1.01.toFloat())
        val res = resources
        mBottomSentences = res.getStringArray(R.array.bottom_sentences_digest)

        if (intYear == 2016 || intYear==2018) {
            when (mCurrentMonthDisplayed) {
                1 -> mHolder.top_month.setImageResource(R.drawable.sk2018_1)
                2 -> mHolder.top_month.setImageResource(R.drawable.sk2018_2)
                3 -> mHolder.top_month.setImageResource(R.drawable.sk2018_3)
                4 -> mHolder.top_month.setImageResource(R.drawable.sk2018_4)
                5 -> mHolder.top_month.setImageResource(R.drawable.sk2018_5)
                6 -> mHolder.top_month.setImageResource(R.drawable.sk2018_6)
                7 -> mHolder.top_month.setImageResource(R.drawable.sk2018_7)
                8 -> mHolder.top_month.setImageResource(R.drawable.sk2018_8)
                9 -> mHolder.top_month.setImageResource(R.drawable.sk2018_9)
                10 -> mHolder.top_month.setImageResource(R.drawable.sk2018_10)
                11 -> mHolder.top_month.setImageResource(R.drawable.sk2018_11)
                12 -> mHolder.top_month.setImageResource(R.drawable.sk2018_12)
            }
            bottom_sentense0.text=mBottomSentences[mCurrentMonthDisplayed+3*(mCurrentMonthDisplayed-1)]
            bottom_sentense1.text=mBottomSentences[mCurrentMonthDisplayed+3*(mCurrentMonthDisplayed-1)+1]
            bottom_sentense2.text=mBottomSentences[mCurrentMonthDisplayed+3*(mCurrentMonthDisplayed-1)+2]
        } else if (intYear == 2017 || intYear==2019) {
            when (mCurrentMonthDisplayed) {
                1 -> mHolder.top_month.setImageResource(R.drawable.sk2019_1)
                2 -> mHolder.top_month.setImageResource(R.drawable.sk2019_2)
                3 -> mHolder.top_month.setImageResource(R.drawable.sk2019_3)
                4 -> mHolder.top_month.setImageResource(R.drawable.sk2019_4)
                5 -> mHolder.top_month.setImageResource(R.drawable.sk2019_5)
                6 -> mHolder.top_month.setImageResource(R.drawable.sk2019_6)
                7 -> mHolder.top_month.setImageResource(R.drawable.sk2019_7)
                8 -> mHolder.top_month.setImageResource(R.drawable.sk2019_8)
                9 -> mHolder.top_month.setImageResource(R.drawable.sk2019_9)
                10 -> mHolder.top_month.setImageResource(R.drawable.sk2019_10)
                11 -> mHolder.top_month.setImageResource(R.drawable.sk2019_11)
                12 -> mHolder.top_month.setImageResource(R.drawable.sk2019_12)
            }
            bottom_sentense0.text=mBottomSentences[mCurrentMonthDisplayed+36+3*(mCurrentMonthDisplayed-1)]
            bottom_sentense1.text=mBottomSentences[mCurrentMonthDisplayed+36+3*(mCurrentMonthDisplayed-1)+1]
            bottom_sentense2.text=mBottomSentences[mCurrentMonthDisplayed+36+3*(mCurrentMonthDisplayed-1)+2]
        } else {
            mHolder.top_month.setImageResource(R.drawable.placeholder)
            bottom_sentense0.text=mBottomSentences[0]
            bottom_sentense1.text=mBottomSentences[1]
            bottom_sentense2.text=mBottomSentences[2]
        }

        dayNumber=mHolder.findViewById(R.id.day_monthly_number)
        dayNumber.text=mCurrentDay+context!!.getString(R.string.days_raw)
        dayNumber.setTextColor(Color.WHITE)
        dayNumber.textSize=mConfig.getFontSize()*1.07f

        mHolder.id_bottom_sentense.setOnClickListener {
            (activity as MainActivity).openQingxinFromMonthly(time)
        }
    }
}
