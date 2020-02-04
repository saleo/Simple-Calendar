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

        val map = HashMap<String, Calendar>()
        map[getSchemeCalendar(year, month, 3, -0xbf24db, "假").toString()] = getSchemeCalendar(year, month, 3, -0xbf24db, "假")
        map[getSchemeCalendar(year, month, 6, -0x196ec8, "事").toString()] = getSchemeCalendar(year, month, 6, -0x196ec8, "事")
        map[getSchemeCalendar(year, month, 9, -0x20ecaa, "议").toString()] = getSchemeCalendar(year, month, 9, -0x20ecaa, "议")
        map[getSchemeCalendar(year, month, 13, -0x123a93, "记").toString()] = getSchemeCalendar(year, month, 13, -0x123a93, "记")
        map[getSchemeCalendar(year, month, 14, -0x123a93, "记").toString()] = getSchemeCalendar(year, month, 14, -0x123a93, "记")
        map[getSchemeCalendar(year, month, 15, -0x5533bc, "假").toString()] = getSchemeCalendar(year, month, 15, -0x5533bc, "假")
        map[getSchemeCalendar(year, month, 18, -0x43ec10, "记").toString()] = getSchemeCalendar(year, month, 18, -0x43ec10, "记")
        map[getSchemeCalendar(year, month, 22, -0x20ecaa, "议").toString()] = getSchemeCalendar(year, month, 22, -0x20ecaa, "议")
        map[getSchemeCalendar(year, month, 25, -0xec5310, "假").toString()] = getSchemeCalendar(year, month, 25, -0xec5310, "假")
        map[getSchemeCalendar(year, month, 27, -0xec5310, "多").toString()] = getSchemeCalendar(year, month, 27, -0xec5310, "多")
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        calendarView.setSchemeDate(map)
    }

    private fun getSchemeCalendar(year: Int, month: Int, day: Int, color: Int, text: String): com.haibin.calendarview.Calendar {
        val calendar = com.haibin.calendarview.Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.schemeColor = color//如果单独标记颜色、则会使用这个颜色
        calendar.scheme = text
        calendar.addScheme(color, "假")
        calendar.addScheme(if (day % 2 == 0) -0xff3300 else -0x2ea012, "节")
        calendar.addScheme(if (day % 2 == 0) -0x9a0000 else -0xbe961f, "记")
        return calendar
    }

    override fun onCalendarOutOfRange(calendar: Calendar) {

    }

    @SuppressLint("SetTextI18n")
    override fun onCalendarSelect(calendar: Calendar, isClick: Boolean) {

        if (isClick) {
            Toasty.custom(activity as Context, getFormattedMessage(), null, resources.getColor(R.color.md_green_100), resources.getColor(R.color.md_green_100), Toasty.LENGTH_SHORT, false, true).show()
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

    private fun getFormattedMessage(): CharSequence {
        val blueForeColor: CharacterStyle
        val redForeColor: CharacterStyle
        blueForeColor = ForegroundColorSpan(Color.BLUE)
        redForeColor = ForegroundColorSpan(Color.RED)

        val word1 = "word1"
        val word2 = "word2"
        val ssb = SpannableStringBuilder(word1).append("\n").append(word2)
        val len_word1 = word1.length
        val len_word2 = word2.length

        ssb.setSpan(blueForeColor, 0, len_word1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        ssb.setSpan(redForeColor, len_word1, len_word1 + len_word2 + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        return ssb
    }
}


