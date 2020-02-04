package net.euse.calendar.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import com.haibin.calendarview.TrunkBranchAnnals
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_month_calendarview.*
import net.euse.calendar.R
import net.euse.calendar.activities.MainActivity
import net.euse.calendar.extensions.dbHelper
import net.euse.calendar.helpers.MONTHLY_VIEW
import org.joda.time.DateTime
import java.util.*

class MonthCalendarViewFragment:Fragment(),CalendarView.OnCalendarSelectListener,CalendarView.OnMonthChangeListener{


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_month_calendarview,container,false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initWindow()
        calendarView.setOnCalendarSelectListener(this)
        calendarView.setOnMonthChangeListener(this)
        initData()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).updateTopBottom(view=MONTHLY_VIEW)
    }

    protected fun initWindow(){

    }

    protected fun initData() {
        val year = calendarView.getCurYear()
        val month = calendarView.getCurMonth()

        var map = HashMap<String, Calendar>()

        activity!!.dbHelper.getAllEventsIntoMap(map)
            //此方法在巨大的数据量上不影响遍历性能，推荐使用
        calendarView.setSchemeDate(map)
    }

    override fun onCalendarOutOfRange(calendar: Calendar) {

    }

    @SuppressLint("SetTextI18n")
    override fun onCalendarSelect(calendar: Calendar, isClick: Boolean) {

        if (isClick) {
            Toasty.custom(activity as Context, getFormattedMessage(calendar), null, resources.getColor(R.color.md_green_100), resources.getColor(R.color.md_green_100), Toasty.LENGTH_SHORT, false, true).show()
        }

        Log.e("onDateSelected", "  -- " + calendar.year +
                "  --  " + calendar.month +
                "  -- " + calendar.day +
                "  --  " + isClick + "  --   " + calendar.scheme)
        Log.e("onDateSelected", "  " + calendarView.getSelectedCalendar().getScheme() +
                "  --  " + calendarView.getSelectedCalendar().isCurrentDay())
        Log.e("干支年纪 ： ", " -- " + TrunkBranchAnnals.getTrunkBranchYear(calendar.lunarCalendar.year))
    }

    override fun onMonthChange(year: Int, month: Int) {
        Log.d(tag,"year=$year...month=$month")
        (activity as MainActivity).updateTopBottom(DateTime().withDate(year,month,1), MONTHLY_VIEW)
    }

    private fun getFormattedMessage(calendar: Calendar): CharSequence {
        var foreColor: CharacterStyle
        var ssb=SpannableStringBuilder("")
        foreColor = ForegroundColorSpan(Color.RED)
        var start=0;var end=0

        calendar.schemes.forEach {
            start=ssb.length
            ssb.append(it.scheme).append("\n")
            end=start+it.scheme.length
            foreColor=ForegroundColorSpan(it.shcemeColor)
            ssb.setSpan(foreColor,start,end,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
//        val word1 = "word1"
//        val word2 = "word2"
//
//        val len_word1 = word1.length
//        val len_word2 = word2.length
//
//        ssb.setSpan(blueForeColor, 0, len_word1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
//        ssb.setSpan(redForeColor, len_word1, len_word1 + len_word2 + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        return ssb
    }
}


