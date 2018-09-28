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
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.R.string.days_raw
import com.simplemobiletools.calendar.activities.EventActivity
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
            mHolder.day_events.adapter = this
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
        val topMonth= mHolder.top_month

        val mbottomsentenceFooter: ViewGroup
        val mBottomSentences: Array<String>
        val mBottomSentence0: TextView
        val mBottomSentence1: TextView
        var dayNumber:TextView

        mbottomsentenceFooter = mHolder.findViewById(R.id.id_bottom_sentense) as  ViewGroup

        mBottomSentence0 = mbottomsentenceFooter.findViewById(R.id.bottom_sentense0) as TextView
        mBottomSentence1 = mbottomsentenceFooter.findViewById(R.id.bottom_sentense1) as TextView
        mBottomSentence0.setTextSize(mConfig.getFontSize()*1.01.toFloat())
        mBottomSentence1.setTextSize(mConfig.getFontSize()*1.01.toFloat())
        val res = resources
        mBottomSentences = res.getStringArray(R.array.bottom_sentences)

        if (intYear == 2016 || intYear==2018) {
            when (mCurrentMonthDisplayed) {
                1 -> {topMonth.setImageResource(R.drawable.sk2016_1)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+1]
                }
                2 -> {topMonth.setImageResource(R.drawable.sk2016_2)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+2]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+3]
                }
                3 -> {topMonth.setImageResource(R.drawable.sk2016_3)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+4]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+5]
                }
                4 -> {topMonth.setImageResource(R.drawable.sk2016_4)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+6]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+7]
                }
                5 -> {topMonth.setImageResource(R.drawable.sk2016_5)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+8]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+9]
                }
                6 -> {topMonth.setImageResource(R.drawable.sk2016_6)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+10]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+11]
                }
                7 -> {topMonth.setImageResource(R.drawable.sk2016_7)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+12]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+13]
                }
                8 -> {topMonth.setImageResource(R.drawable.sk2016_8)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+14]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+15]
                }
                9 -> {topMonth.setImageResource(R.drawable.sk2016_9)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+16]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+17]
                }
                10 -> {topMonth.setImageResource(R.drawable.sk2016_10)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+18]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+19]
                }
                11 -> {topMonth.setImageResource(R.drawable.sk2016_11)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+20]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+21]
                }
                12 -> {topMonth.setImageResource(R.drawable.sk2016_12)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+22]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+23]
                }
            }
        } else if (intYear == 2017) {
            when (mCurrentMonthDisplayed) {
                1 -> {topMonth.setImageResource(R.drawable.sk2017_1)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+24]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+25]
                }
                2 -> {topMonth.setImageResource(R.drawable.sk2017_2)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+26]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+27]
                }
                3 -> {topMonth.setImageResource(R.drawable.sk2017_3)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+28]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+29]
                }
                4 -> {topMonth.setImageResource(R.drawable.sk2017_4)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+30]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+31]
                }
                5 -> {topMonth.setImageResource(R.drawable.sk2017_5)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+32]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+33]
                }
                6 -> {topMonth.setImageResource(R.drawable.sk2017_6)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+34]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+35]
                }
                7 -> {topMonth.setImageResource(R.drawable.sk2017_7)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+36]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+37]
                }
                8 -> {topMonth.setImageResource(R.drawable.sk2017_8)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+38]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+39]
                }
                9 -> {topMonth.setImageResource(R.drawable.sk2017_9)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+40]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+41]
                }
                10 -> {topMonth.setImageResource(R.drawable.sk2017_10)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+42]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+43]
                }
                11 -> {topMonth.setImageResource(R.drawable.sk2017_11)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+44]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+45]
                }
                12 -> {topMonth.setImageResource(R.drawable.sk2017_12)
                    mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+46]
                    mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+47]
                }
            }
        } else {
            topMonth.setImageResource(R.drawable.placeholder)
            mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+0]
            mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+1]
        }

        dayNumber=mHolder.findViewById(R.id.day_monthly_number)
        dayNumber.text=mCurrentDay
        dayNumber.setTextColor(Color.WHITE)
        dayNumber.textSize=mConfig.getFontSize()*1.08f
    }
}
