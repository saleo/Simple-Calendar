package com.simplemobiletools.calendar.extensions

import android.accounts.Account
import android.annotation.SuppressLint
import android.app.*
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.ContentObserver
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.simplemobiletools.calendar.*
import com.simplemobiletools.calendar.R.drawable.today_border
import com.simplemobiletools.calendar.activities.*
import com.simplemobiletools.calendar.helpers.*
import com.simplemobiletools.calendar.helpers.Formatter
import com.simplemobiletools.calendar.models.*
import com.simplemobiletools.calendar.receivers.CalDAVSyncReceiver
import com.simplemobiletools.calendar.receivers.NotificationReceiver
import com.simplemobiletools.calendar.services.PostponeService
import com.simplemobiletools.calendar.services.SnoozeService
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.getFilePublicUri
import com.simplemobiletools.commons.extensions.onGlobalLayout
import com.simplemobiletools.commons.helpers.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.dbHelper: DBHelper get() = DBHelper.newInstance(applicationContext)

fun Context.getNowSeconds() = (System.currentTimeMillis() / 1000).toInt()

fun Context.updateWidgets() {
    val widgetsCnt = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, MyWidgetMonthlyProvider::class.java))
    if (widgetsCnt.isNotEmpty()) {
        val ids = intArrayOf(R.xml.widget_monthly_info)
        Intent(applicationContext, MyWidgetMonthlyProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(this)
        }
    }

    updateListWidget()
}

fun Context.updateListWidget() {
    val widgetsCnt = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, MyWidgetListProvider::class.java))
    if (widgetsCnt.isNotEmpty()) {
        val ids = intArrayOf(R.xml.widget_list_info)
        Intent(applicationContext, MyWidgetListProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(this)
        }
    }
}

fun Context.scheduleAllEvents() {
    val events = dbHelper.getEventsAfterward()
    events.forEach {
        scheduleNextEventReminder(it, dbHelper)
    }
}

fun Context.scheduleNextEventReminder(event: Event?, dbHelper: DBHelper) {
    if (event == null || event.getReminders().isEmpty()) {
        return
    }

    val now = getNowSeconds()
    val reminderSeconds = event.getReminders().reversed().map { it * 60 }
    dbHelper.getEvents(now, now + YEAR, event.id) {
        if (it.isNotEmpty()) {
            for (curEvent in it) {
                for (curReminder in reminderSeconds) {
                    if (curEvent.getEventStartTS() - curReminder > now) {
                        scheduleEventIn((curEvent.getEventStartTS() - curReminder) * 1000L, curEvent)
                        return@getEvents
                    }
                }
            }
        }
    }
}

fun Context.scheduleEventIn(notifTS: Long, event: Event) {
    if (notifTS < System.currentTimeMillis()) {
        return
    }

    val pendingIntent = getNotificationIntent(applicationContext, event)
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    when {
        isMarshmallowPlus() -> alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifTS, pendingIntent)
        isKitkatPlus() -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifTS, pendingIntent)
        else -> alarmManager.set(AlarmManager.RTC_WAKEUP, notifTS, pendingIntent)
    }
}

fun Context.cancelAllNotification(){
    val ntfIDs=config.getNtfIdsAsList()
    for (i in 0 until ntfIDs.size) {
        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        PendingIntent.getBroadcast(applicationContext, ntfIDs[i].toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT).cancel()
    }
    config.ntfIDs=""
}

fun Context.cancelNotification(eventStartTs:Int) {
    dbHelper.getEvents(eventStartTs,eventStartTs,-1,{
        if (it.size>0)
            processEventRemindersNotification(it as ArrayList<Event>)
        else{
            val ntfId=eventStartTs/com.simplemobiletools.calendar.helpers.DAY_SECONDS
            val intent = Intent(applicationContext, NotificationReceiver::class.java)
            PendingIntent.getBroadcast(applicationContext, ntfId, intent, PendingIntent.FLAG_UPDATE_CURRENT).cancel()
        }
    })
}

private fun getNotificationIntent(context: Context, event: Event): PendingIntent {
    val intent = Intent(context, NotificationReceiver::class.java)
    intent.putExtra(EVENT_ID, event.id)
    intent.putExtra(EVENT_OCCURRENCE_TS, event.startTS)
    return PendingIntent.getBroadcast(context, event.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

private fun getGroupedNotificationIntent(context: Context,ntfId:Int,ntfTitle:String,ntfContent:String,ntfTS:Long):PendingIntent{
    val intent = Intent(context, NotificationReceiver::class.java)
    intent.putExtra(NOTIFICATION_ID,ntfId)
    intent.putExtra(NOTIFICATION_TITLE,ntfTitle)
    intent.putExtra(NOTIFICATION_CONTENT,ntfContent)
    intent.putExtra(NOTIFICATION_TS,ntfTS)

    return PendingIntent.getBroadcast(context, ntfId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

private fun getPostponeNotificationIntent(context: Context,ntfId:Int,ntfTitle:String,ntfContent:String,ntfTS:Long):PendingIntent{
    val intent = Intent(context, PostponeService::class.java)
    intent.putExtra(NOTIFICATION_ID,ntfId)
    intent.putExtra(NOTIFICATION_TITLE,ntfTitle)
    intent.putExtra(NOTIFICATION_CONTENT,ntfContent)
    intent.putExtra(NOTIFICATION_TS,ntfTS)

    return PendingIntent.getService(context, ntfId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun Context.getRepetitionText(seconds: Int) = when (seconds) {
    0 -> getString(R.string.no_repetition)
    com.simplemobiletools.calendar.helpers.DAY_SECONDS -> getString(R.string.daily)
    WEEK -> getString(R.string.weekly)
    MONTH -> getString(R.string.monthly)
    YEAR -> getString(R.string.yearly)
    else -> {
        when {
            seconds % YEAR == 0 -> resources.getQuantityString(R.plurals.years, seconds / YEAR, seconds / YEAR)
            seconds % MONTH == 0 -> resources.getQuantityString(R.plurals.months, seconds / MONTH, seconds / MONTH)
            seconds % WEEK == 0 -> resources.getQuantityString(R.plurals.weeks, seconds / WEEK, seconds / WEEK)
            else -> resources.getQuantityString(R.plurals.days, seconds / com.simplemobiletools.calendar.helpers.DAY_SECONDS, seconds / com.simplemobiletools.calendar.helpers.DAY_SECONDS)
        }
    }
}

fun Context.getFilteredEvents(events: List<Event>): List<Event> {
    val displayEventTypes = config.displayEventTypes
    return events.filter { displayEventTypes.contains(it.eventType.toString()) }
}

fun Context.notifyRunningEvents() {
    dbHelper.getRunningEvents().forEach { notifyEvent(it) }
}

fun Context.notifyEvent(event: Event) {
    val pendingIntent = getPendingIntent(applicationContext, event)
    val startTime = Formatter.getTimeFromTS(applicationContext, event.startTS)
    val endTime = Formatter.getTimeFromTS(applicationContext, event.endTS)
    val timeRange = if (event.getIsAllDay()) getString(R.string.all_day) else getFormattedEventTime(startTime, endTime)
    val descriptionOrLocation = if (config.replaceDescription) event.location else event.description
    val notification = getNotification(applicationContext, pendingIntent, event, "$timeRange $descriptionOrLocation",event.title)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(event.id, notification)
}

fun Context.postGroupedNotify(ntfId:Int,ntfTitle:String,ntfContent:String,ntfTS:Long){
    val pendingIntent = getPendingIntentWithGroupedNtfId(applicationContext, ntfId)

    val notification= buildGroupedNotification(applicationContext,pendingIntent,ntfId,ntfTitle,ntfContent,ntfTS)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(ntfId, notification) //notification id always set to event-startTS, which make all the event with the same startTS has the same notificationId

}

@SuppressLint("NewApi")
private fun buildGroupedNotification(context: Context, pendingIntent: PendingIntent, ntfId:Int,ntfTitle: String,ntfContent:String,ntfTS:Long): Notification {
    val channelId = "reminder_channel"
    if (isOreoPlus()) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = context.resources.getString(R.string.event_reminders)
        val importance = NotificationManager.IMPORTANCE_HIGH
        NotificationChannel(channelId, name, importance).apply {
            enableLights(true)
            lightColor = Color.RED
            enableVibration(false)
            notificationManager.createNotificationChannel(this)
        }
    }

    var soundUri = Uri.parse(context.config.reminderSound)
    if (soundUri.scheme == "file") {
        try {
            soundUri = context.getFilePublicUri(File(soundUri.path), BuildConfig.APPLICATION_ID)
        } catch (ignored: Exception) {
        }
    }

    val content= if (ntfContent == "," || ntfContent== "") context.getString(R.string.str_notification_content) else ntfContent
    val builder = NotificationCompat.Builder(context)
            .setContentTitle(ntfTitle)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setChannelId(channelId)
            .addAction(R.drawable.ic_snooze, context.getString(R.string.snooze), getPostponeNotificationIntent(context, ntfId,ntfTitle,content,ntfTS))


    if (isLollipopPlus()) {
        builder.setVisibility(Notification.VISIBILITY_PUBLIC)
    }

    if (context.config.vibrateOnReminder) {
        builder.setVibrate(longArrayOf(0, 300, 300, 300))
    }

    return builder.build()
}


@SuppressLint("NewApi")
private fun getNotification(context: Context, pendingIntent: PendingIntent, event: Event, content: String,title:String): Notification {
    val channelId = "reminder_channel"
    if (isOreoPlus()) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = context.resources.getString(R.string.event_reminders)
        val importance = NotificationManager.IMPORTANCE_HIGH
        NotificationChannel(channelId, name, importance).apply {
            enableLights(true)
            lightColor = event.color
            enableVibration(false)
            notificationManager.createNotificationChannel(this)
        }
    }

    var soundUri = Uri.parse(context.config.reminderSound)
    if (soundUri.scheme == "file") {
        try {
            soundUri = context.getFilePublicUri(File(soundUri.path), BuildConfig.APPLICATION_ID)
        } catch (ignored: Exception) {
        }
    }

    val builder = NotificationCompat.Builder(context)
            .setContentTitle(event.title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setChannelId(channelId)
            .addAction(R.drawable.ic_snooze, context.getString(R.string.snooze), getSnoozePendingIntent(context, event))

    if (isLollipopPlus()) {
        builder.setVisibility(Notification.VISIBILITY_PUBLIC)
    }

    if (context.config.vibrateOnReminder) {
        builder.setVibrate(longArrayOf(0, 300, 300, 300))
    }

    return builder.build()
}

private fun getFormattedEventTime(startTime: String, endTime: String) = if (startTime == endTime) startTime else "$startTime - $endTime"

private fun getPendingIntent(context: Context, event: Event): PendingIntent {
    val intent = Intent(context, EventActivity::class.java)
    intent.putExtra(EVENT_ID, event.id)
    intent.putExtra(EVENT_OCCURRENCE_TS, event.startTS)
    return PendingIntent.getActivity(context, event.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

private fun getPendingIntentWithGroupedNtfId(context: Context,groupedNtfId:Int):PendingIntent{
    val intent = Intent(context, MainActivity::class.java)
    val dayCode =Formatter.getDayCodeFromTS(groupedNtfId* com.simplemobiletools.commons.helpers.DAY_SECONDS)
    intent.putExtra(DAY_CODE,dayCode)
    return PendingIntent.getActivity(context, groupedNtfId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

private fun getSnoozePendingIntent(context: Context, event: Event): PendingIntent {
    val snoozeClass = if (context.config.useSameSnooze) SnoozeService::class.java else SnoozeReminderActivity::class.java
    val intent = Intent(context, snoozeClass).setAction("Snoozeee")
    intent.putExtra(EVENT_ID, event.id)
    return if (context.config.useSameSnooze) {
        PendingIntent.getService(context, event.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    } else {
        PendingIntent.getActivity(context, event.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}

fun Context.rescheduleReminder(event: Event?, minutes: Int) {
    if (event != null) {
        applicationContext.scheduleEventIn(System.currentTimeMillis() + minutes * 60000, event)
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(event.id)
    }
}

fun Context.launchNewEventIntent(dayCode: String = Formatter.getTodayCode(this)) {
    Intent(applicationContext, EventActivity::class.java).apply {
        putExtra(NEW_EVENT_START_TS, getNewEventTimestampFromCode(dayCode))
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(this)
    }
}

fun Context.getNewEventTimestampFromCode(dayCode: String): Int {
    val currHour = DateTime(System.currentTimeMillis(), DateTimeZone.getDefault()).hourOfDay
    val dateTime = Formatter.getLocalDateTimeFromCode(dayCode).withHourOfDay(currHour)
    val newDateTime = dateTime.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
    // make sure the date doesn't change
    return newDateTime.withDate(dateTime.year, dateTime.monthOfYear, dateTime.dayOfMonth).seconds()
}

fun Context.getCurrentOffset() = SimpleDateFormat("Z", Locale.getDefault()).format(Date())

fun Context.getSyncedCalDAVCalendars() = CalDAVHandler(applicationContext).getCalDAVCalendars(null, config.caldavSyncedCalendarIDs)

fun Context.recheckCalDAVCalendars(callback: () -> Unit) {
    if (config.caldavSync) {
        Thread {
            CalDAVHandler(applicationContext).refreshCalendars(null, callback)
            updateWidgets()
        }.start()
    }
}

fun Context.scheduleCalDAVSync(activate: Boolean) {
    val syncIntent = Intent(applicationContext, CalDAVSyncReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, syncIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (activate) {
        val syncCheckInterval = 4 * AlarmManager.INTERVAL_HOUR
        try {
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + syncCheckInterval, syncCheckInterval, pendingIntent)
        } catch (ignored: SecurityException) {
        }
    } else {
        alarm.cancel(pendingIntent)
    }
}

fun Context.syncCalDAVCalendars(activity: SimpleActivity?, calDAVSyncObserver: ContentObserver) {
    Thread {
        val uri = CalendarContract.Calendars.CONTENT_URI
        contentResolver.unregisterContentObserver(calDAVSyncObserver)
        contentResolver.registerContentObserver(uri, false, calDAVSyncObserver)

        val accounts = HashSet<Account>()
        val calendars = CalDAVHandler(applicationContext).getCalDAVCalendars(activity, config.caldavSyncedCalendarIDs)
        calendars.forEach {
            accounts.add(Account(it.accountName, it.accountType))
        }

        Bundle().apply {
            putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
            putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            accounts.forEach {
                ContentResolver.requestSync(it, uri.authority, this)
            }
        }
    }.start()
}

fun Context.addDayNumber(rawTextColor: Int, day: DayMonthly, linearLayout: LinearLayout, dayLabelHeight: Int, callback: (Int) -> Unit) {
    var textColor = rawTextColor
    var solarTerm=""

    (View.inflate(applicationContext, R.layout.day_monthly_number_view, null) as TextView).apply {
        text = day.value.toString()
        setTextColor(Color.BLACK)
        textSize = (context.config.getFontSize()) * 1.2.toFloat()
        gravity = Gravity.TOP or Gravity.LEFT
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        linearLayout.addView(this)

        if (day.isToday) {
            val primaryColor = getAdjustedPrimaryColor()
            setTextColor(Color.BLACK)
            if (dayLabelHeight == 0) {
                onGlobalLayout {
                    val height = this@apply.height
                    if (height > 0) {
                        callback(height)
                    }
                }
            }
            linearLayout.setBackgroundResource(today_border)
        }

    }
    //show chinese lunar when in zh-cn,or zh-tw,etc
    val strCountry = resources.configuration.locale.country
    if (strCountry == "CN" || strCountry == "TW") {
        val calendar = Calendar.getInstance()
        val datetime=Formatter.getDateTimeFromCode(day.code)
        val year=datetime.year
        val month=datetime.monthOfYear-1
        calendar.set(year,month,day.value)
        val lunar = Lunar(calendar, applicationContext)
        solarTerm = SolarTerm.getSolarTermStr(year, month, day.value, applicationContext)
        if (solarTerm.length == 0) {
            val SolarHoliDayStr = SolarHoliday.getSolarHoliday(month, day.value, applicationContext)
            if (SolarHoliDayStr.length == 0) {
                val fullchinadatestr = lunar.toString()
                val LunarFestivalStr = LunarFestival.getLunarFestival(fullchinadatestr, lunar, applicationContext)
                if (LunarFestivalStr.length != 0) {
                    solarTerm=LunarFestivalStr
                } else {
                    solarTerm = fullchinadatestr.substring(fullchinadatestr.length - 2, fullchinadatestr.length)
                }
            } else {
                solarTerm=SolarHoliDayStr
            }
        }

    }

//    if (isBold) {
//        mMonthNumPaint.setFakeBoldText(isBold = false)
//    }

    (View.inflate(applicationContext, R.layout.day_monthly_number_lunar_view, null) as TextView).apply {
        setTextColor(Color.BLACK)
        text = solarTerm
        gravity = Gravity.BOTTOM or Gravity.LEFT
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        linearLayout.addView(this)
    }

}

private fun addTodaysBackground(textView: TextView, res: Resources, dayLabelHeight: Int, mPrimaryColor: Int) =
        textView.addResizedBackgroundDrawable(res, dayLabelHeight, mPrimaryColor, R.drawable.monthly_today_circle)

fun Context.addDayEvents(day: DayMonthly, linearLayout: LinearLayout, res: Resources, dividerMargin: Int) {
    val eventLayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    var eventBackgroundColor=Color.GRAY
    var eventText=""
    run loop@{
        day.dayEvents.sortedWith(compareBy({ it.color })).forEach {
            if (it.color != Color.GRAY) {
                eventBackgroundColor = it.color;eventText = it.title
                if (eventBackgroundColor == Color.RED) return@loop
            }
            else{
                if (eventBackgroundColor!=Color.BLUE) {
                    eventBackgroundColor = it.color;eventText = it.title
                }
            }
        }
    }
    if (eventText != "") {
        val backgroundDrawable = res.getDrawable(R.drawable.day_monthly_event_background)
        backgroundDrawable.applyColorFilter(eventBackgroundColor)
        eventLayoutParams.setMargins(dividerMargin, 0, dividerMargin, dividerMargin)

        (View.inflate(applicationContext, R.layout.day_monthly_event_view, null) as TextView).apply {
            setTextColor(Color.WHITE)
            text = eventText.replace(" ", "\u00A0")  // allow word break by char
            background = backgroundDrawable
            layoutParams = eventLayoutParams
            linearLayout.addView(this)
        }
    }

}

fun Context.getEventListItems(events: List<Event>): ArrayList<ListItem> {
    val listItems = ArrayList<ListItem>(events.size)
    val replaceDescription = config.replaceDescription
    val sorted = events.sortedWith(compareBy({ it.startTS }, { it.endTS }, { it.title }, { if (replaceDescription) it.location else it.description }))
    val sublist = sorted.subList(0, Math.min(sorted.size, 100))
    var prevCode = ""
    sublist.forEach {
        val code = Formatter.getDayCodeFromTS(it.startTS)
        if (code != prevCode) {
            val day = Formatter.getDayTitle(this, code)
            listItems.add(ListSection(day, code))
            prevCode = code
        }
        listItems.add(ListEvent(it.id, it.startTS, it.endTS, it.title, it.description, it.getIsAllDay(), it.color, it.location))
    }
    return listItems
}

fun Context.processEventRemindersNotification(eventsToProcess:ArrayList<Event>){
    val idsToProcess = ArrayList<String>()
    var currentNotifyTs=0
    val now = getNowSeconds()

    eventsToProcess.forEach{
        currentNotifyTs=it.startTS+config.reminderTs
        if (currentNotifyTs>now) {
            idsToProcess.add(it.id.toString())
        }
    }


    if (idsToProcess.size>0){
        processEventRemindersNotification(idsToProcess)
        Log.d(APP_TAG,"reminders notification processed")
    }
    else
        Log.d(APP_TAG,"reminders notification NOT processed,for no data")
}

fun Context.processEventRemindersNotification(eventIdsToProcess:ArrayList<String>, notifyTms: Long=0, inNtfId:Int=0, inNtfTitle:String="", inNtfContent:String="") {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    var ntfIDs=ArrayList<Int>()
    val existNtfIds=config.getNtfIdsAsList()
    if (inNtfId == 0) {
        val gns = dbHelper.getGroupedNotifications(eventIdsToProcess)
        gns.forEach {
            val pendingIntent = getGroupedNotificationIntent(this, it.ntfId, it.ntfTitle, it.ntfContent,it.ntfTms)
            when {
                isMarshmallowPlus() -> alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, it.ntfTms, pendingIntent)
                isKitkatPlus() -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, it.ntfTms, pendingIntent)
                else -> alarmManager.set(AlarmManager.RTC_WAKEUP, it.ntfTms, pendingIntent)
            }
            if (!existNtfIds.contains(it.ntfId.toString()))
                ntfIDs.add(it.ntfId)
        }
        if (existNtfIds.size>0)
            config.ntfIDs=config.ntfIDs+","+TextUtils.join(",",ntfIDs)
        else
            config.ntfIDs=TextUtils.join(",",ntfIDs)
    }
    else {
        //for postponed notification
        val pendingIntent = getGroupedNotificationIntent(this, inNtfId, inNtfTitle, inNtfContent, notifyTms)
        when {
            isMarshmallowPlus() -> alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifyTms, pendingIntent)
            isKitkatPlus() -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyTms, pendingIntent)
            else -> alarmManager.set(AlarmManager.RTC_WAKEUP, notifyTms, pendingIntent)
        }
    }
}

