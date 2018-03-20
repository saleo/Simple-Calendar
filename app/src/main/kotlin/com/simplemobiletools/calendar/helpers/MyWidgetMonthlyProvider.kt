package com.simplemobiletools.calendar.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.View
import android.widget.ImageView
import android.widget.RemoteViews
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.SplashActivity
import com.simplemobiletools.calendar.extensions.config
import com.simplemobiletools.calendar.extensions.launchNewEventIntent
import com.simplemobiletools.calendar.interfaces.MonthlyCalendar
import com.simplemobiletools.calendar.models.DayMonthly
import com.simplemobiletools.commons.extensions.*
import org.joda.time.DateTime

class MyWidgetMonthlyProvider : AppWidgetProvider() {
    private val PREV = "prev"
    private val NEXT = "next"
    private val NEW_EVENT = "new_event"

    companion object {
        private var targetDate = DateTime.now()
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        performUpdate(context)
    }

    private fun performUpdate(context: Context) {
        MonthlyCalendarImpl(monthlyCalendar, context).getMonth(targetDate)
    }

    private fun getComponentName(context: Context) = ComponentName(context, MyWidgetMonthlyProvider::class.java)

    private fun setupIntent(context: Context, views: RemoteViews, action: String, id: Int) {
        Intent(context, MyWidgetMonthlyProvider::class.java).apply {
            this.action = action
            val pendingIntent = PendingIntent.getBroadcast(context, 0, this, 0)
            views.setOnClickPendingIntent(id, pendingIntent)
        }
    }

    private fun setupAppOpenIntent(context: Context, views: RemoteViews, id: Int, dayCode: String) {
        Intent(context, SplashActivity::class.java).apply {
            putExtra(DAY_CODE, dayCode)
            putExtra(OPEN_MONTH, true)
            val pendingIntent = PendingIntent.getActivity(context, Integer.parseInt(dayCode.substring(0, 6)), this, 0)
            views.setOnClickPendingIntent(id, pendingIntent)
        }
    }

    private fun setupDayOpenIntent(context: Context, views: RemoteViews, id: Int, dayCode: String) {
        Intent(context, SplashActivity::class.java).apply {
            putExtra(DAY_CODE, dayCode)
            val pendingIntent = PendingIntent.getActivity(context, Integer.parseInt(dayCode), this, 0)
            views.setOnClickPendingIntent(id, pendingIntent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            PREV -> getPrevMonth(context)
            NEXT -> getNextMonth(context)
            NEW_EVENT -> context.launchNewEventIntent()
            else -> super.onReceive(context, intent)
        }
    }

    private fun getPrevMonth(context: Context) {
        targetDate = targetDate!!.minusMonths(1)
        MonthlyCalendarImpl(monthlyCalendar, context).getMonth(targetDate!!)
    }

    private fun getNextMonth(context: Context) {
        targetDate = targetDate!!.plusMonths(1)
        MonthlyCalendarImpl(monthlyCalendar, context).getMonth(targetDate!!)
    }

    private fun updateDays(context: Context, views: RemoteViews, days: List<DayMonthly>) {
        val displayWeekNumbers = context.config.displayWeekNumbers
        val textColor = context.config.widgetTextColor
        val smallerFontSize = context.config.getFontSize() - 3f
        val res = context.resources
        val len = days.size
        val packageName = context.packageName
        views.apply {
            setTextColor(R.id.week_num, textColor)
            setTextSize(R.id.week_num, smallerFontSize)
            setViewVisibility(R.id.week_num, if (displayWeekNumbers) View.VISIBLE else View.GONE)
        }

        for (i in 0..5) {
            val id = res.getIdentifier("week_num_$i", "id", packageName)
            views.apply {
                setText(id, "${days[i * 7 + 3].weekOfYear}:")    // fourth day of the week matters at determining week of the year
                setTextColor(id, textColor)
                setTextSize(id, smallerFontSize)
                setViewVisibility(id, if (displayWeekNumbers) View.VISIBLE else View.GONE)
            }
        }

        val weakTextColor = textColor.adjustAlpha(LOW_ALPHA)
        for (i in 0 until len) {
            val day = days[i]
            val currTextColor = if (day.isThisMonth) textColor else weakTextColor
            val id = res.getIdentifier("day_$i", "id", packageName)
            views.removeAllViews(id)
            addDayNumber(context, views, day, currTextColor, id)
            setupDayOpenIntent(context, views, id, day.code)

            day.dayEvents.forEach {
                var backgroundColor = it.color
                var eventTextColor = backgroundColor.getContrastColor()

                if (!day.isThisMonth) {
                    eventTextColor = eventTextColor.adjustAlpha(0.25f)
                    backgroundColor = backgroundColor.adjustAlpha(0.25f)
                }

                val newRemoteView = RemoteViews(packageName, R.layout.day_monthly_event_view).apply {
                    setText(R.id.day_monthly_event_id, it.title.replace(" ", "\u00A0"))
                    setTextColor(R.id.day_monthly_event_id, eventTextColor)
                    setTextSize(R.id.day_monthly_event_id, smallerFontSize - 3f)
                    setBackgroundColor(R.id.day_monthly_event_id, backgroundColor)
                }
                views.addView(id, newRemoteView)
            }
        }
    }

    private fun addDayNumber(context: Context, views: RemoteViews, day: DayMonthly, textColor: Int, id: Int) {
        val newRemoteView = RemoteViews(context.packageName, R.layout.day_monthly_number_view).apply {
            setText(R.id.day_monthly_number_id, day.value.toString())
            setTextSize(R.id.day_monthly_number_id, context.config.getFontSize() - 3f)

            if (day.isToday) {
                setBackgroundColor(R.id.day_monthly_number_id, textColor)
                setTextColor(R.id.day_monthly_number_id, textColor.getContrastColor())
            } else {
                setTextColor(R.id.day_monthly_number_id, textColor)
            }
        }
        views.addView(id, newRemoteView)
    }

    private val monthlyCalendar = object : MonthlyCalendar {
        override fun updateMonthlyCalendar(context: Context, month: String, days: List<DayMonthly>, checkedEvents: Boolean) {
            val largerFontSize = context.config.getFontSize() + 3f
            val textColor = context.config.widgetTextColor
            val resources = context.resources

            val appWidgetManager = AppWidgetManager.getInstance(context)
            appWidgetManager.getAppWidgetIds(getComponentName(context)).forEach {
                val views = RemoteViews(context.packageName, R.layout.fragment_month_widget)
                setMonthDisplayed(views,DateTime.now())

                views.setBackgroundColor(R.id.calendar_holder, context.config.widgetBgColor)

                var bmp =  resources.getColoredBitmap(R.drawable.ic_plus, textColor)
                views.setImageViewBitmap(R.id.top_new_event, bmp)
                updateDayLabels(context, views, resources, textColor)
                updateDays(context, views, days)

//                val monthCode = days.firstOrNull { it.code.substring(6) == "01" }?.code ?: Formatter.getTodayCode(context)
//                setupAppOpenIntent(context, views, R.id.top_value, monthCode)

                appWidgetManager.updateAppWidget(it, views)
            }
        }
    }

    private fun updateDayLabels(context: Context, views: RemoteViews, resources: Resources, textColor: Int) {
        val sundayFirst = context.config.isSundayFirst
        val smallerFontSize = context.config.getFontSize()
        val packageName = context.packageName
        val letters = letterIDs
        for (i in 0..6) {
            val id = resources.getIdentifier("label_$i", "id", packageName)
            views.setTextColor(id, textColor)
            views.setTextSize(id, smallerFontSize)

            var index = i
            if (!sundayFirst) {
                index = (index + 1) % letters.size
            }

            views.setText(id, resources.getString(letters[index]))
        }
    }

    /**
     * Sets the month displayed at the top of this view based on time. Override
     * to add custom events when the title is changed.
     *
     * @param time A day in the new focus month.
     * @param updateHighlight TODO(epastern):
     */
    protected fun setMonthDisplayed(views: RemoteViews, time: DateTime) {

        //        CharSequence oldMonth = mMonthName.getText();
        //        mMonthName.setText(Utils.formatMonthYear(mContext, time));
        //        mMonthName.invalidate();
        //        if (!TextUtils.equals(oldMonth, mMonthName.getText())) {
        //            mMonthName.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        //        }
        val intYear = time.year
        val mCurrentMonthDisplayed = time.monthOfYear
        val view:ImageView

        if (intYear == 2016) {
            when (mCurrentMonthDisplayed) {
                0 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_1)
                1 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_2)
                2 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_3)
                3 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_4)
                4 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_5)
                5 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_6)
                6 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_7)
                7 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_8)
                8 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_9)
                9 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_10)
                10 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_11)
                11 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_12)
                else -> views.setImageViewResource(R.id.top_month,R.drawable.sk2016_1)
            }
        } else if (intYear == 2017) {
            when (mCurrentMonthDisplayed) {
                0 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_1)
                1 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_2)
                2 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_3)
                3 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_4)
                4 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_5)
                5 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_6)
                6 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_7)
                7 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_8)
                8 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_9)
                9 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_10)
                10 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_11)
                11 -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_12)
                else -> views.setImageViewResource(R.id.top_month,R.drawable.sk2017_1)
            }
        } else {
            views.setImageViewResource(R.id.top_month,R.drawable.placeholder)
        }
    }

}
