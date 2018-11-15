package com.simplemobiletools.calendar.fragments

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.extensions.addDayEvents
import com.simplemobiletools.calendar.extensions.addDayNumber
import com.simplemobiletools.calendar.extensions.config
import com.simplemobiletools.calendar.helpers.*
import com.simplemobiletools.calendar.interfaces.MonthlyCalendar
import com.simplemobiletools.calendar.interfaces.NavigationListener
import com.simplemobiletools.calendar.models.DayMonthly
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getDialogTheme
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.first_row.*
import kotlinx.android.synthetic.main.fragment_month.view.*
import org.joda.time.DateTime


class MonthFragment : Fragment(), MonthlyCalendar {
    private var mTextColor = 0
    private var mSundayFirst = false
    private var mDayCode = ""
    private var mPackageName = ""
    private var mDayLabelHeight = 0
    private var mLastHash = 0L
    private var mCalendar: MonthlyCalendarImpl? = null

    var listener: NavigationListener? = null

    lateinit var mRes: Resources
    lateinit var mHolder: RelativeLayout
    lateinit var mConfig: Config

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_month, container, false)
        mRes = resources
        mPackageName = activity!!.packageName
        mHolder = view.rl_monthcalendar_holder
        mDayCode = arguments!!.getString(DAY_CODE)
        mConfig = context!!.config
        mSundayFirst = mConfig.isSundayFirst

        mTextColor = mConfig.textColor
        setupLabels()
        mCalendar = MonthlyCalendarImpl(this, context!!)

        return view
    }

    override fun onPause() {
        super.onPause()
        mSundayFirst = context!!.config.isSundayFirst
    }

    override fun onResume() {
        super.onResume()
        if (mConfig.isSundayFirst != mSundayFirst) {
            mSundayFirst = mConfig.isSundayFirst
            setupLabels()
        }

        mCalendar!!.apply {
            mTargetDate = Formatter.getDateTimeFromCode(mDayCode)
            getDays(false)    // prefill the screen asap, even if without events
        }

        updateCalendar()
    }

    fun updateCalendar() {
        mCalendar?.updateMonthlyCalendar(Formatter.getDateTimeFromCode(mDayCode))
    }

    override fun updateMonthlyCalendar(context: Context, month: String, days: List<DayMonthly>, checkedEvents: Boolean) {
        val newHash = month.hashCode() + days.hashCode().toLong()
        if ((mLastHash != 0L && !checkedEvents) || mLastHash == newHash) {
            return
        }

        mLastHash = newHash


        activity?.runOnUiThread {
            updateDays(days)
            (activity as MainActivity).updateContentBasedMonth(Formatter.getDateTimeFromCode(mDayCode),mHolder)

        }
    }

//    private fun setupButtons() {
//        mTextColor = mConfig.textColor
//
//        mHolder.top_left_arrow.apply {
//            applyColorFilter(mTextColor)
//            background = null
//            setOnClickListener {
//                listener?.goLeft()
//            }
//        }
//
//        mHolder.top_right_arrow.apply {
//            applyColorFilter(mTextColor)
//            background = null
//            setOnClickListener {
//                listener?.goRight()
//            }
//        }
//
//        mHolder.top_value.apply {
//            setTextColor(mConfig.textColor)
//            setOnClickListener {
//                showMonthDialog()
//            }
//        }
//    }

    private fun showMonthDialog() {
        activity!!.setTheme(context!!.getDialogTheme())
        val view = layoutInflater.inflate(R.layout.date_picker, null)
        val datePicker = view.findViewById<DatePicker>(R.id.date_picker)
        datePicker.findViewById<View>(Resources.getSystem().getIdentifier("day", "id", "android")).beGone()

        val dateTime = DateTime(mCalendar!!.mTargetDate.toString())
        datePicker.init(dateTime.year, dateTime.monthOfYear - 1, 1, null)

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
        val newDateTime = dateTime.withDate(year, month, 1)
        listener?.goToDateTime(newDateTime)
    }

    private fun setupLabels() {
        val letters = letterIDs

        for (i in 0..6) {
            var index = i
            if (!mSundayFirst)
                index = (index + 1) % letters.size

            mHolder.findViewById<TextView>(mRes.getIdentifier("label_$i", "id", mPackageName)).apply {
                setTextColor(mTextColor)
                text = getString(letters[index])
            }
        }
    }


    private fun updateDays(days: List<DayMonthly>) {
        val displayWeekNumbers = mConfig.displayWeekNumbers
        val len = days.size

        if (week_num == null)
            return

        week_num.setTextColor(mTextColor)
        week_num.beVisibleIf(displayWeekNumbers)

        for (i in 0..4) {
            mHolder.findViewById<TextView>(mRes.getIdentifier("week_num_$i", "id", mPackageName)).apply {
                text = "${days[i * 7 + 3].weekOfYear}:"     // fourth day of the week matters
                setTextColor(mTextColor)
                beVisibleIf(displayWeekNumbers)
            }
        }

        val dividerMargin = mRes.displayMetrics.density.toInt()
        for (i in 0 until len) {
            mHolder.findViewById<LinearLayout>(mRes.getIdentifier("day_$i", "id", mPackageName)).apply {
                if (days[i].isThisMonth)
                 {
                    val day = days[i]
                    if (day.dayEvents.size>0)
                        setOnClickListener {
                            (activity as MainActivity).openDayFromMonthly(Formatter.getDateTimeFromCode(day.code))
                        }

                    removeAllViews()
                    context.addDayNumber(mTextColor, day, this, mDayLabelHeight) { mDayLabelHeight = it }
                    context.addDayEvents(day, this, mRes, dividerMargin)
                }
            }
        }
    }
    
}
