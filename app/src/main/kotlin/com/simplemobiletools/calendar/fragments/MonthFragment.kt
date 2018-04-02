package com.simplemobiletools.calendar.fragments

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.MainActivity
import com.simplemobiletools.calendar.extensions.addDayEvents
import com.simplemobiletools.calendar.extensions.addDayNumber
import com.simplemobiletools.calendar.extensions.config
import com.simplemobiletools.calendar.helpers.*
import com.simplemobiletools.calendar.helpers.Formatter
import com.simplemobiletools.calendar.interfaces.MonthlyCalendar
import com.simplemobiletools.calendar.interfaces.NavigationListener
import com.simplemobiletools.calendar.models.DayMonthly
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.first_row.*
import kotlinx.android.synthetic.main.fragment_month.view.*
import kotlinx.android.synthetic.main.top_navigation.view.*
import org.joda.time.DateTime
import android.widget.TextView



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
        mHolder = view.month_calendar_holder
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
            updateMonth(Formatter.getDateTimeFromCode(mDayCode))
            updateDays(days)
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

        for (i in 0..5) {
            mHolder.findViewById<TextView>(mRes.getIdentifier("week_num_$i", "id", mPackageName)).apply {
                text = "${days[i * 7 + 3].weekOfYear}:"     // fourth day of the week matters
                setTextColor(mTextColor)
                beVisibleIf(displayWeekNumbers)
            }
        }

        val dividerMargin = mRes.displayMetrics.density.toInt()
        for (i in 0 until len) {
            mHolder.findViewById<LinearLayout>(mRes.getIdentifier("day_$i", "id", mPackageName)).apply {
                val day = days[i]
                setOnClickListener {
                    (activity as MainActivity).openDayFromMonthly(Formatter.getDateTimeFromCode(day.code))
                }

                removeAllViews()
                context.addDayNumber(mTextColor, day, this, mDayLabelHeight) { mDayLabelHeight = it }
                context.addDayEvents(day, this, mRes, dividerMargin)
            }
        }
    }
    
    private fun updateMonth(time: DateTime)
    {
        

            //        CharSequence oldMonth = mMonthName.getText();
            //        mMonthName.setText(Utils.formatMonthYear(mContext, time));
            //        mMonthName.invalidate();
            //        if (!TextUtils.equals(oldMonth, mMonthName.getText())) {
            //            mMonthName.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            //        }
            val intYear = time.year
            val mCurrentMonthDisplayed = time.monthOfYear
            val view: ImageView

            val mbottomsentenceFooter: ViewGroup
            val mBottomSentences: Array<String>
            val mBottomSentence0: TextView
            val mBottomSentence1: TextView

        mbottomsentenceFooter = mHolder.findViewById(R.id.id_bottom_sentense) as  ViewGroup
        mBottomSentence0 = mbottomsentenceFooter.findViewById(R.id.bottom_sentense0) as TextView
        mBottomSentence1 = mbottomsentenceFooter.findViewById(R.id.bottom_sentense1) as TextView
        val res = resources
        mBottomSentences = res.getStringArray(R.array.bottom_sentences)

            if (intYear == 2016 || intYear==2018) {
                when (mCurrentMonthDisplayed) {
                    1 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_1)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+1]
                    }
                    2 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_2)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+2]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+3]
                    }
                    3 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_3)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+4]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+5]
                    }
                    4 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_4)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+6]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+7]
                    }
                    5 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_5)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+8]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+9]
                    }
                    6 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_6)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+10]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+11]
                    }                     
                    7 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_7)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+12]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+13]
                    }                        
                    8 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_8)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+14]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+15]
                    }                        
                    9 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_9)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+16]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+17]
                    }                        
                    10 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_10)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+18]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+19]
                    }                        
                    11 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_11)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+20]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+21]
                    }                        
                    12 -> {mHolder.top_month.setImageResource(R.drawable.sk2016_12)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+22]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+23]
                    }                        
                }
            } else if (intYear == 2017) {
                when (mCurrentMonthDisplayed) {
                    1 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_1)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+24]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+25]
                    }                        
                    2 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_2)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+26]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+27]
                    }                        
                    3 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_3)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+28]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+29]
                    }                        
                    4 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_4)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+30]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+31]
                    }                        
                    5 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_5)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+32]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+33]
                    }                        
                    6 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_6)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+34]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+35]
                    }                        
                    7 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_7)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+36]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+37]
                    }                        
                    8 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_8)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+38]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+39]
                    }                        
                    9 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_9)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+40]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+41]
                    }                        
                    10 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_10)
                            mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+42]
                            mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+43]
                        }                            
                    11 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_11)
                            mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+44]
                            mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+45]
                        }                            
                    12 -> {mHolder.top_month.setImageResource(R.drawable.sk2017_12)
                        mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+46]
                        mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+47]
                    }                        
                }
            } else {
                mHolder.top_month.setImageResource(R.drawable.placeholder)
                mBottomSentence0.text=mBottomSentences[mCurrentMonthDisplayed+0]
                mBottomSentence1.text=mBottomSentences[mCurrentMonthDisplayed+1]
            }
    }
}
