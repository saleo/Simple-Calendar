package net.euse.calendar.fragments

import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.SparseIntArray
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import net.euse.calendar.R
import net.euse.calendar.activities.EventActivity
import net.euse.calendar.extensions.config
import net.euse.calendar.extensions.dbHelper
import net.euse.calendar.extensions.getFilteredEvents
import net.euse.calendar.extensions.seconds
import net.euse.calendar.helpers.*
import net.euse.calendar.helpers.Formatter
import net.euse.calendar.interfaces.WeekFragmentListener
import net.euse.calendar.interfaces.WeeklyCalendar
import net.euse.calendar.models.Event
import net.euse.calendar.views.MyScrollView
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.DAY_SECONDS
import kotlinx.android.synthetic.main.fragment_week.*
import kotlinx.android.synthetic.main.fragment_week.view.*
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*

class WeekFragment : androidx.fragment.app.Fragment(), WeeklyCalendar {
    private val CLICK_DURATION_THRESHOLD = 150
    private val PLUS_FADEOUT_DELAY = 5000L

    var mListener: WeekFragmentListener? = null
    private var mWeekTimestamp = 0
    private var mRowHeight = 0
    private var minScrollY = -1
    private var maxScrollY = -1
    private var mWasDestroyed = false
    private var primaryColor = 0
    private var lastHash = 0
    private var isFragmentVisible = false
    private var wasFragmentInit = false
    private var wasExtraHeightAdded = false
    private var clickStartTime = 0L
    private var selectedGrid: View? = null
    private var todayColumnIndex = -1
    private var events = ArrayList<Event>()
    private var allDayHolders = ArrayList<RelativeLayout>()
    private var allDayRows = ArrayList<HashSet<Int>>()
    private var eventTypeColors = SparseIntArray()

    lateinit var inflater: LayoutInflater
    lateinit var mView: View
    lateinit var mScrollView: MyScrollView
    lateinit var mCalendar: WeeklyCalendarImpl
    lateinit var mRes: Resources

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context!!.dbHelper.getEventTypes {
            it.map { eventTypeColors.put(it.id, it.color) }
        }

        mRowHeight = (context!!.resources.getDimension(R.dimen.weekly_view_row_height)).toInt()
        minScrollY = mRowHeight * context!!.config.startWeeklyAt
        mWeekTimestamp = arguments!!.getInt(WEEK_START_TIMESTAMP)
        primaryColor = context!!.getAdjustedPrimaryColor()
        mRes = resources
        allDayRows.add(HashSet())
        mCalendar = WeeklyCalendarImpl(this, context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.inflater = inflater

        mView = inflater.inflate(R.layout.fragment_week, container, false)
        mScrollView = mView.week_events_scrollview
        mScrollView.setOnScrollviewListener(object : MyScrollView.ScrollViewListener {
            override fun onScrollChanged(scrollView: MyScrollView, x: Int, y: Int, oldx: Int, oldy: Int) {
                checkScrollLimits(y)
            }
        })

        mScrollView.onGlobalLayout {
            updateScrollY(Math.max(mListener?.getCurrScrollY() ?: 0, minScrollY))
        }

        (0..6).map { inflater.inflate(R.layout.stroke_vertical_divider, mView.week_vertical_grid_holder) }
        (0..23).map { inflater.inflate(R.layout.stroke_horizontal_divider, mView.week_horizontal_grid_holder) }

        wasFragmentInit = true
        return mView
    }

    override fun onPause() {
        super.onPause()
        wasExtraHeightAdded = true
    }

    override fun onResume() {
        super.onResume()
        setupDayLabels()
        updateCalendar()

        mScrollView.onGlobalLayout {
            if (context == null) {
                return@onGlobalLayout
            }

            minScrollY = mRowHeight * context!!.config.startWeeklyAt
            maxScrollY = mRowHeight * context!!.config.endWeeklyAt

            val bounds = Rect()
            week_events_holder.getGlobalVisibleRect(bounds)
            maxScrollY -= bounds.bottom - bounds.top
            if (minScrollY > maxScrollY)
                maxScrollY = -1

            checkScrollLimits(mScrollView.scrollY)
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        isFragmentVisible = menuVisible
        if (isFragmentVisible && wasFragmentInit) {
            mListener?.updateHoursTopMargin(mView.week_top_holder.height)
            checkScrollLimits(mScrollView.scrollY)
        }
    }

    fun updateCalendar() {
        mCalendar.updateWeeklyCalendar(mWeekTimestamp)
    }

    private fun setupDayLabels() {
        var curDay = Formatter.getDateTimeFromTS(mWeekTimestamp)
        val textColor = context!!.config.textColor
        val todayCode = Formatter.getDayCodeFromDateTime(DateTime())
        for (i in 0..6) {
            val dayCode = Formatter.getDayCodeFromDateTime(curDay)
            val dayLetter = getDayLetter(curDay.dayOfWeek)
            mView.findViewById<TextView>(mRes.getIdentifier("week_day_label_$i", "id", context!!.packageName)).apply {
                text = "$dayLetter\n${curDay.dayOfMonth}"
                setTextColor(if (todayCode == dayCode) primaryColor else textColor)
                if (todayCode == dayCode) {
                    todayColumnIndex = i
                }
            }
            curDay = curDay.plusDays(1)
        }
    }

    private fun getDayLetter(pos: Int): String {
        return mRes.getString(when (pos) {
            1 -> R.string.monday_letter
            2 -> R.string.tuesday_letter
            3 -> R.string.wednesday_letter
            4 -> R.string.thursday_letter
            5 -> R.string.friday_letter
            6 -> R.string.saturday_letter
            else -> R.string.sunday_letter
        })
    }

    private fun checkScrollLimits(y: Int) {
        if (minScrollY != -1 && y < minScrollY) {
            mScrollView.scrollY = minScrollY
        } else if (maxScrollY != -1 && y > maxScrollY) {
            mScrollView.scrollY = maxScrollY
        } else if (isFragmentVisible) {
            mListener?.scrollTo(y)
        }
    }

    private fun initGrid() {
        (0..6).map { getColumnWithId(it) }
                .forEachIndexed { index, layout ->
                    layout.removeAllViews()
                    layout.setOnTouchListener { view, motionEvent ->
                        checkGridClick(motionEvent, index, layout)
                        true
                    }
                }
    }

    private fun checkGridClick(event: MotionEvent, index: Int, view: ViewGroup) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> clickStartTime = System.currentTimeMillis()
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - clickStartTime < CLICK_DURATION_THRESHOLD) {
                    selectedGrid?.animation?.cancel()
                    selectedGrid?.beGone()

                    val rowHeight = resources.getDimension(R.dimen.weekly_view_row_height)
                    val hour = (event.y / rowHeight).toInt()
                    selectedGrid = (inflater.inflate(R.layout.week_grid_item, null, false) as ImageView).apply {
                        view.addView(this)
                        background = ColorDrawable(primaryColor)
                        layoutParams.width = view.width
                        layoutParams.height = rowHeight.toInt()
                        y = hour * rowHeight
                        applyColorFilter(primaryColor.getContrastColor())

                        setOnClickListener {
                            val timestamp = mWeekTimestamp + index * DAY_SECONDS + hour * 60 * 60
                            Intent(context, EventActivity::class.java).apply {
                                putExtra(NEW_EVENT_START_TS, timestamp)
                                putExtra(NEW_EVENT_SET_HOUR_DURATION, true)
                                startActivity(this)
                            }
                        }
                        animate().alpha(0f).setStartDelay(PLUS_FADEOUT_DELAY).withEndAction {
                            beGone()
                        }
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun updateWeeklyCalendar(events: ArrayList<Event>) {
        val newHash = events.hashCode()
        if (newHash == lastHash) {
            return
        }

        lastHash = newHash
        this.events = events
        updateEvents()
    }

    private fun updateEvents() {
        if (mWasDestroyed) {
            return
        }

        activity!!.runOnUiThread {
            if (context != null && isAdded) {
                addEvents()
            }
        }
    }

    private fun addEvents() {
        val filtered = context!!.getFilteredEvents(events)

        initGrid()
        allDayHolders.clear()
        allDayRows.clear()
        allDayRows.add(HashSet())
        week_all_day_holder?.removeAllViews()

        addNewLine()

        val fullHeight = mRes.getDimension(R.dimen.weekly_view_events_height)
        val minuteHeight = fullHeight / (24 * 60)
        val minimalHeight = mRes.getDimension(R.dimen.weekly_view_minimal_event_height).toInt()

        var hadAllDayEvent = false
        val replaceDescription = context!!.config.replaceDescription
        val sorted = filtered.sortedWith(compareBy({ it.startTS }, { it.endTS }, { it.title }, { if (replaceDescription) it.location else it.description }))
        for (event in sorted) {
            if (event.getIsAllDay() || Formatter.getDayCodeFromTS(event.startTS) != Formatter.getDayCodeFromTS(event.endTS)) {
                hadAllDayEvent = true
                addAllDayEvent(event)
            } else {
                val startDateTime = Formatter.getDateTimeFromTS(event.startTS)
                val endDateTime = Formatter.getDateTimeFromTS(event.endTS)
                val dayOfWeek = startDateTime.plusDays(if (context!!.config.isSundayFirst) 1 else 0).dayOfWeek - 1
                val layout = getColumnWithId(dayOfWeek)

                val startMinutes = startDateTime.minuteOfDay
                val duration = endDateTime.minuteOfDay - startMinutes

                (inflater.inflate(R.layout.week_event_marker, null, false) as TextView).apply {
                    val backgroundColor = eventTypeColors.get(event.eventType, primaryColor)
                    background = ColorDrawable(backgroundColor)
                    setTextColor(backgroundColor.getContrastColor())
                    text = event.title
                    layout.addView(this)
                    y = startMinutes * minuteHeight
                    (layoutParams as RelativeLayout.LayoutParams).apply {
                        width = layout.width - 1
                        minHeight = if (event.startTS == event.endTS) minimalHeight else (duration * minuteHeight).toInt() - 1
                    }
                    setOnClickListener {
                        Intent(context, EventActivity::class.java).apply {
                            putExtra(EVENT_ID, event.id)
                            putExtra(EVENT_OCCURRENCE_TS, event.startTS)
                            startActivity(this)
                        }
                    }
                }
            }
        }

        if (!hadAllDayEvent) {
            checkTopHolderHeight()
        }

        addCurrentTimeIndicator(minuteHeight)
    }

    private fun addNewLine() {
        val allDaysLine = inflater.inflate(R.layout.all_day_events_holder_line, null, false) as RelativeLayout
        week_all_day_holder.addView(allDaysLine)
        allDayHolders.add(allDaysLine)
    }

    private fun addCurrentTimeIndicator(minuteHeight: Float) {
        if (todayColumnIndex != -1) {
            val minutes = DateTime().minuteOfDay
            val todayColumn = getColumnWithId(todayColumnIndex)
            (inflater.inflate(R.layout.week_now_marker, null, false) as ImageView).apply {
                applyColorFilter(primaryColor)
                mView.week_events_holder.addView(this, 0)
                val extraWidth = (todayColumn.width * 0.3).toInt()
                val markerHeight = resources.getDimension(R.dimen.weekly_view_now_height).toInt()
                (layoutParams as RelativeLayout.LayoutParams).apply {
                    width = todayColumn.width + extraWidth
                    height = markerHeight
                }
                x = todayColumn.x - extraWidth / 2
                y = minutes * minuteHeight - markerHeight / 2
            }
        }
    }

    private fun checkTopHolderHeight() {
        mView.week_top_holder.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mView.week_top_holder.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (isFragmentVisible && activity != null) {
                    mListener?.updateHoursTopMargin(mView.week_top_holder.height)
                }
            }
        })
    }

    private fun addAllDayEvent(event: Event) {
        (inflater.inflate(R.layout.week_all_day_event_marker, null, false) as TextView).apply {
            if (activity == null)
                return

            val backgroundColor = eventTypeColors.get(event.eventType, primaryColor)
            background = ColorDrawable(backgroundColor)
            setTextColor(backgroundColor.getContrastColor())
            text = event.title

            val startDateTime = Formatter.getDateTimeFromTS(event.startTS)
            val endDateTime = Formatter.getDateTimeFromTS(event.endTS)

            val minTS = Math.max(startDateTime.seconds(), mWeekTimestamp)
            val maxTS = Math.min(endDateTime.seconds(), mWeekTimestamp + WEEK_SECONDS)
            val startDateTimeInWeek = Formatter.getDateTimeFromTS(minTS)
            val firstDayIndex = (startDateTimeInWeek.dayOfWeek - if (context!!.config.isSundayFirst) 0 else 1) % 7
            val daysCnt = Days.daysBetween(Formatter.getDateTimeFromTS(minTS).toLocalDate(), Formatter.getDateTimeFromTS(maxTS).toLocalDate()).days

            var doesEventFit: Boolean
            val cnt = allDayRows.size - 1
            var wasEventHandled = false
            var drawAtLine = 0
            for (index in 0..cnt) {
                doesEventFit = true
                drawAtLine = index
                val row = allDayRows[index]
                for (i in firstDayIndex..firstDayIndex + daysCnt) {
                    if (row.contains(i)) {
                        doesEventFit = false
                    }
                }

                for (dayIndex in firstDayIndex..firstDayIndex + daysCnt) {
                    if (doesEventFit) {
                        row.add(dayIndex)
                        wasEventHandled = true
                    } else if (index == cnt) {
                        if (allDayRows.size == index + 1) {
                            allDayRows.add(HashSet<Int>())
                            addNewLine()
                            drawAtLine++
                            wasEventHandled = true
                        }
                        allDayRows.last().add(dayIndex)
                    }
                }
                if (wasEventHandled) {
                    break
                }
            }

            allDayHolders[drawAtLine].addView(this)
            (layoutParams as RelativeLayout.LayoutParams).apply {
                topMargin = mRes.getDimension(R.dimen.tiny_margin).toInt()
                leftMargin = getColumnWithId(firstDayIndex).x.toInt()
                bottomMargin = 1
                width = getColumnWithId(Math.min(firstDayIndex + daysCnt, 6)).right - leftMargin - 1
            }

            calculateExtraHeight()

            setOnClickListener {
                Intent(context, EventActivity::class.java).apply {
                    putExtra(EVENT_ID, event.id)
                    putExtra(EVENT_OCCURRENCE_TS, event.startTS)
                    startActivity(this)
                }
            }
        }
    }

    private fun calculateExtraHeight() {
        mView.week_top_holder.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (activity == null)
                    return

                mView.week_top_holder.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (isFragmentVisible) {
                    mListener?.updateHoursTopMargin(mView.week_top_holder.height)
                }

                if (!wasExtraHeightAdded) {
                    maxScrollY += mView.week_all_day_holder.height
                    wasExtraHeightAdded = true
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mWasDestroyed = true
    }

    private fun getColumnWithId(id: Int) = mView.findViewById<ViewGroup>(mRes.getIdentifier("week_column_$id", "id", context!!.packageName))

    fun updateScrollY(y: Int) {
        if (wasFragmentInit) {
            mScrollView.scrollY = y
        }
    }
}
