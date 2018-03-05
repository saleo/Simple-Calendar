package net.euse.skcal.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.View
import android.widget.RemoteViews
import net.euse.skcal.extensions.config
import net.euse.skcal.extensions.launchNewEventIntent
import net.euse.skcal.interfaces.MonthlyCalendar
import net.euse.skcal.models.DayMonthly
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
        Intent(context, net.euse.skcal.activities.SplashActivity::class.java).apply {
            putExtra(DAY_CODE, dayCode)
            putExtra(OPEN_MONTH, true)
            val pendingIntent = PendingIntent.getActivity(context, Integer.parseInt(dayCode.substring(0, 6)), this, 0)
            views.setOnClickPendingIntent(id, pendingIntent)
        }
    }

    private fun setupDayOpenIntent(context: Context, views: RemoteViews, id: Int, dayCode: String) {
        Intent(context, net.euse.skcal.activities.SplashActivity::class.java).apply {
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
            setTextColor(net.euse.skcal.R.id.week_num, textColor)
            setTextSize(net.euse.skcal.R.id.week_num, smallerFontSize)
            setViewVisibility(net.euse.skcal.R.id.week_num, if (displayWeekNumbers) View.VISIBLE else View.GONE)
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

                val newRemoteView = RemoteViews(packageName, net.euse.skcal.R.layout.day_monthly_event_view).apply {
                    setText(net.euse.skcal.R.id.day_monthly_event_id, it.title.replace(" ", "\u00A0"))
                    setTextColor(net.euse.skcal.R.id.day_monthly_event_id, eventTextColor)
                    setTextSize(net.euse.skcal.R.id.day_monthly_event_id, smallerFontSize - 3f)
                    setBackgroundColor(net.euse.skcal.R.id.day_monthly_event_id, backgroundColor)
                }
                views.addView(id, newRemoteView)
            }
        }
    }

    private fun addDayNumber(context: Context, views: RemoteViews, day: DayMonthly, textColor: Int, id: Int) {
        val newRemoteView = RemoteViews(context.packageName, net.euse.skcal.R.layout.day_monthly_number_view).apply {
            setText(net.euse.skcal.R.id.day_monthly_number_id, day.value.toString())
            setTextSize(net.euse.skcal.R.id.day_monthly_number_id, context.config.getFontSize() - 3f)

            if (day.isToday) {
                setBackgroundColor(net.euse.skcal.R.id.day_monthly_number_id, textColor)
                setTextColor(net.euse.skcal.R.id.day_monthly_number_id, textColor.getContrastColor())
            } else {
                setTextColor(net.euse.skcal.R.id.day_monthly_number_id, textColor)
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
                val views = RemoteViews(context.packageName, net.euse.skcal.R.layout.fragment_month_widget)
                views.setText(net.euse.skcal.R.id.top_value, month)

                views.setBackgroundColor(net.euse.skcal.R.id.calendar_holder, context.config.widgetBgColor)

                views.setTextColor(net.euse.skcal.R.id.top_value, textColor)
                views.setTextSize(net.euse.skcal.R.id.top_value, largerFontSize)

                var bmp = resources.getColoredBitmap(net.euse.skcal.R.drawable.ic_pointer_left, textColor)
                views.setImageViewBitmap(net.euse.skcal.R.id.top_left_arrow, bmp)

                bmp = resources.getColoredBitmap(net.euse.skcal.R.drawable.ic_pointer_right, textColor)
                views.setImageViewBitmap(net.euse.skcal.R.id.top_right_arrow, bmp)

                bmp = resources.getColoredBitmap(net.euse.skcal.R.drawable.ic_plus, textColor)
                views.setImageViewBitmap(net.euse.skcal.R.id.top_new_event, bmp)
                updateDayLabels(context, views, resources, textColor)
                updateDays(context, views, days)

                setupIntent(context, views, PREV, net.euse.skcal.R.id.top_left_arrow)
                setupIntent(context, views, NEXT, net.euse.skcal.R.id.top_right_arrow)
                setupIntent(context, views, NEW_EVENT, net.euse.skcal.R.id.top_new_event)

                val monthCode = days.firstOrNull { it.code.substring(6) == "01" }?.code ?: Formatter.getTodayCode(context)
                setupAppOpenIntent(context, views, net.euse.skcal.R.id.top_value, monthCode)

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
}
