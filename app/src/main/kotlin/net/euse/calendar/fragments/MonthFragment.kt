package net.euse.calendar.fragments

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.getDialogTheme
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.fragment_month.*
import net.euse.calendar.R
import net.euse.calendar.activities.MainActivity
import net.euse.calendar.extensions.addDayEvents
import net.euse.calendar.extensions.addDayNumber
import net.euse.calendar.extensions.addSpecialSolarTermBottom
import net.euse.calendar.extensions.config
import net.euse.calendar.helpers.*
import net.euse.calendar.interfaces.MonthlyCalendar
import net.euse.calendar.interfaces.NavigationListener
import net.euse.calendar.models.DayMonthly
import org.joda.time.DateTime


class MonthFragment : androidx.fragment.app.Fragment(), MonthlyCalendar {
    private var mTextColor = 0
    private var mSundayFirst = false
    private var mDayCode = ""
    private var mPackageName = ""
    private var mDayLabelHeight = 0
    private var mSolarTermIndex =0
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
        mDayCode = arguments!!.getString(DAY_CODE)
        mConfig = context!!.config
        mSundayFirst = mConfig.isSundayFirst

        mTextColor = mConfig.textColor
        mCalendar = MonthlyCalendarImpl(this, context!!)

        return view
    }

    override fun onPause() {
        super.onPause()
        mSundayFirst = context!!.config.isSundayFirst
    }

    override fun onResume() {
        super.onResume()
        mHolder = rl_monthcalendar_holder
        setupLabels()

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
        val len = days.size
        var isSpecialSolarTerm=false
        val dividerMargin = mRes.displayMetrics.density.toInt()
        for (i in 0 until len) {
            mHolder.findViewById<LinearLayout>(mRes.getIdentifier("day_$i", "id", mPackageName)).apply {
                if (days[i].isThisMonth)
                 {
                    val day = days[i]
                    if (day.dayEvents.size>0)
                        setOnClickListener {
                            val bundle=Bundle()
                            bundle.putString(DAY_CODE,day.code)
                            ((activity as MainActivity).currentFragments.last() as MonthFragmentsHolder).arguments=bundle
                            (activity as MainActivity).openFragmentHolder(Formatter.getDateTimeFromCode(day.code), DAILY_VIEW)
                        }

                    removeAllViews()
                    context.addDayNumber(mTextColor, day, this) { mSolarTermIndex=it }
                    context.addDayEvents(day, this, mRes, dividerMargin)

                     if (mSolarTermIndex== XIAZHI_INDEX || mSolarTermIndex== DONGZHI_INDEX ||
                             mSolarTermIndex== LICHUN_INDEX || mSolarTermIndex== LIXIA_INDEX ||
                             mSolarTermIndex== LIQIU_INDEX || mSolarTermIndex== LIDONG_INDEX||
                             mSolarTermIndex== CHUNFEN_INDEX || mSolarTermIndex== QIUFEN_INDEX) {

                         isSpecialSolarTerm=true
                         context.addSpecialSolarTermBottom(this,mRes )
                     }else if (mSolarTermIndex == ZHI_RELEVANT_DAY || mSolarTermIndex == FEN_RELEVANT_DAY || mSolarTermIndex == LI_RELEVANT_DAY) {
                         isSpecialSolarTerm = true
                     }else
                         isSpecialSolarTerm=false

                     if (day.isToday)
                         this.setBackgroundResource(R.drawable.today_border)
                     else if (isSpecialSolarTerm)
                         this.setBackgroundResource(R.drawable.special_solarterm_border)

                }
            }
        }
    }
    
}
