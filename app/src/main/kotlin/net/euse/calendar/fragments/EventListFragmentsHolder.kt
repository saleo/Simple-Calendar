package net.euse.calendar.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.euse.calendar.R
import net.euse.calendar.activities.MainActivity
import net.euse.calendar.adapters.MyEventListPagerAdapter
import net.euse.calendar.extensions.config
import net.euse.calendar.helpers.DAY_CODE
import net.euse.calendar.helpers.EVENTS_LIST_VIEW
import net.euse.calendar.helpers.Formatter
import net.euse.calendar.interfaces.NavigationListener
import com.simplemobiletools.commons.views.MyViewPager
import kotlinx.android.synthetic.main.fragment_eventlist_holder.view.*
import org.joda.time.DateTime
import java.util.*

class EventListFragmentsHolder : MyFragmentHolder(), NavigationListener {
    private val PREFILLED_MONTHS = 97

    private var viewPager: MyViewPager? = null
    private var defaultMonthlyPage = 0
    private var todayDayCode = ""
    private var isGoToTodayVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentDayCode = arguments?.getString(DAY_CODE) ?: ""
        todayDayCode = Formatter.getTodayCode(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_eventlist_holder, container, false)
        view.background = ColorDrawable(context!!.config.backgroundColor)
        viewPager = view.fragment_eventlist_viewpager
        viewPager!!.id = (System.currentTimeMillis() % 100000).toInt()
        setupFragment()
        return view
    }

    private fun setupFragment() {
        val codes = getMonths(currentDayCode)
        val monthlyAdapter = MyEventListPagerAdapter(activity!!.supportFragmentManager, codes, this)
        defaultMonthlyPage = codes.size / 2


        viewPager!!.apply {
            adapter = monthlyAdapter
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    currentDayCode = codes[position]
                    (activity as? MainActivity)?.apply{
                        updateTopBottom(Formatter.getDateTimeFromCode(currentDayCode), EVENTS_LIST_VIEW)
                        refreshCalDAVCalendars(showRefreshToast)
                    }
                }
            })
            currentItem = defaultMonthlyPage
        }
        updateActionBarTitle()
    }

    private fun getMonths(code: String): List<String> {
        val months = ArrayList<String>(PREFILLED_MONTHS)
        val today = Formatter.getDateTimeFromCode(code)
        for (i in -PREFILLED_MONTHS / 2..PREFILLED_MONTHS / 2) {
            months.add(Formatter.getDayCodeFromDateTime(today.plusMonths(i)))
        }
        return months
    }

    override fun goLeft() {
        viewPager!!.currentItem = viewPager!!.currentItem - 1
    }

    override fun goRight() {
        viewPager!!.currentItem = viewPager!!.currentItem + 1
    }

    override fun goToDateTime(dateTime: DateTime) {
        currentDayCode = Formatter.getDayCodeFromDateTime(dateTime)
        setupFragment()
    }

    override fun goToToday() {
        currentDayCode = todayDayCode
        setupFragment()
    }

    override fun refreshEvents() {
        (viewPager?.adapter as? MyEventListPagerAdapter)?.updateMonthlyEventLists(viewPager?.currentItem ?: 0)
    }

    override fun shouldGoToTodayBeVisible() = currentDayCode != todayDayCode

    override fun updateActionBarTitle() {
        (activity as MainActivity).supportActionBar?.title = getString(R.string.app_launcher_name)
    }

    override fun getNewEventDayCode() = currentDayCode
}
