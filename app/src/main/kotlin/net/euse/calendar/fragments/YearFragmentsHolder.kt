package net.euse.calendar.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.euse.calendar.R
import net.euse.calendar.activities.MainActivity
import net.euse.calendar.adapters.MyYearPagerAdapter
import net.euse.calendar.extensions.config
import net.euse.calendar.helpers.Formatter
import com.simplemobiletools.commons.views.MyViewPager
import kotlinx.android.synthetic.main.fragment_years_holder.view.*
import org.joda.time.DateTime

class YearFragmentsHolder : MyFragmentHolder() {
    private val PREFILLED_YEARS = 31

    private var viewPager: MyViewPager? = null
    private var defaultYearlyPage = 0
    private var todayYear = 0
    private var currentYear = 0
    private var isGoToTodayVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentYear = DateTime().toString(Formatter.YEAR_PATTERN).toInt()
        todayYear = currentYear
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_years_holder, container, false)
        view.background = ColorDrawable(context!!.config.backgroundColor)
        viewPager = view.fragment_years_viewpager
        viewPager!!.id = (System.currentTimeMillis() % 100000).toInt()
        setupFragment()
        return view
    }

    private fun setupFragment() {
        val years = getYears(currentYear)
        val yearlyAdapter = MyYearPagerAdapter(activity!!.supportFragmentManager, years)
        defaultYearlyPage = years.size / 2

        viewPager?.apply {
            adapter = yearlyAdapter
            addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    currentYear = years[position]
                    val shouldGoToTodayBeVisible = shouldGoToTodayBeVisible()
                    if (isGoToTodayVisible != shouldGoToTodayBeVisible) {
                        (activity as? MainActivity)?.toggleGoToTodayVisibility(shouldGoToTodayBeVisible)
                        isGoToTodayVisible = shouldGoToTodayBeVisible
                    }

                    if (position < years.size) {
                        (activity as? MainActivity)?.supportActionBar?.title = "${getString(R.string.app_launcher_name)} - ${years[position]}"
                    }
                }
            })
            currentItem = defaultYearlyPage
        }
        updateActionBarTitle()
    }

    private fun getYears(targetYear: Int): List<Int> {
        val years = ArrayList<Int>(PREFILLED_YEARS)
        years += targetYear - PREFILLED_YEARS / 2..targetYear + PREFILLED_YEARS / 2
        return years
    }

    override fun goToToday() {
        currentYear = todayYear
        setupFragment()
    }

    override fun refreshEvents() {
        (viewPager?.adapter as? MyYearPagerAdapter)?.updateCalendars(viewPager?.currentItem ?: 0)
    }

    override fun shouldGoToTodayBeVisible() = currentYear != todayYear

    override fun updateActionBarTitle() {
        (activity as? MainActivity)?.supportActionBar?.title = "${getString(R.string.app_launcher_name)} - $currentYear"
    }

    override fun getNewEventDayCode() = Formatter.getTodayCode(context!!)
}
