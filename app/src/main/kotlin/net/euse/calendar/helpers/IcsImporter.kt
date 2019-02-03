package net.euse.calendar.helpers

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import at.bitfire.icsdroid.Constants
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.extensions.toast
import net.euse.calendar.R
import net.euse.calendar.activities.MainActivity
import net.euse.calendar.activities.SimpleActivity
import net.euse.calendar.extensions.config
import net.euse.calendar.extensions.dbHelper
import net.euse.calendar.models.Event
import net.euse.calendar.models.EventType
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

class IcsImporter(val activity: SimpleActivity):AsyncTask<Void,String,Boolean>() {
    enum class ImportResult {
        IMPORT_FAIL, IMPORT_OK, IMPORT_PARTIAL,IMPORT_IGNORED
    }

    private val SKCAL_URL="https://rili.euse.net/sk_events.ics"

    private var curStart = -1
    private var curEnd = -1
    private var curTitle = ""
    private var curDescription = ""
    private var curImportId = ""
    private var curFlags = 0
    private var curReminderMinutes = ArrayList<Int>()
    private var curRepeatExceptions = ArrayList<Int>()
    private var curRepeatInterval = 0
    private var curRepeatLimit = 0
    private var curRepeatRule = 0
    private var curEventType = DBHelper.REGULAR_EVENT_TYPE_ID
    private var curLastModified = 0L
    private var curLocation = ""
    private var curCategoryColor = ""
    private var isNotificationDescription = false
    private var isProperReminderAction = false
    private var isDescription = false
    private var curReminderTriggerMinutes = -1

    private var eventsImported = 0
    private var eventsFailed = 0
    private var eventsTotal=0

    override fun doInBackground(vararg params: Void?): Boolean {
        var url=URL(SKCAL_URL)
        var conn: URLConnection?=null
        var inputStream:InputStream?

        var followRedirect = false
        var redirect = 0

        var result=false

        publishProgress(activity.getString(R.string.downloading_importing)+"-1")

        do {
            try {
                conn = url.openConnection()

                if (conn is HttpURLConnection) {
                    conn.setRequestProperty("User-Agent", Constants.USER_AGENT)
                    conn.setRequestProperty("Connection", "close")  // workaround for AndroidHttpClient bug, which causes "Unexpected Status Line" exceptions
                    conn.instanceFollowRedirects = false

                    val statusCode = conn.responseCode


                    // handle redirects
                    val location = conn.getHeaderField("Location")
                    if (statusCode/100 == 3 && location != null) {
                        conn.disconnect()   // don't read input stream
                        conn = null

                        Log.i(APP_TAG, "Following redirect to $location")
                        url = URL(url, location)
                        followRedirect = true
                        if (statusCode == HttpURLConnection.HTTP_MOVED_PERM) {
                            Log.i(APP_TAG, "Permanent redirect: saving new location")
                        }
                    }

                    // only read stream if status is 200 OK
                    if (conn is HttpURLConnection && statusCode != HttpURLConnection.HTTP_OK) {
                        conn.disconnect()
                        conn = null
                        val str=String.format(activity.getString(R.string.connectin_failed_with_statuscode),statusCode)
                        activity.showErrorToast(str)
                        return false
                    }
                } else
                // local file, always simulate HTTP status 200 OK
                    requireNotNull(conn)

            } catch(e: IOException) {
                Log.e(APP_TAG, "Couldn't fetch calendar", e)
                activity.showErrorToast(e, Toast.LENGTH_LONG)
            } catch(e: Exception) {
                Log.e(APP_TAG, "network or other failure", e)
            }
            redirect++
        } while (followRedirect && redirect < Constants.MAX_REDIRECTS)

        publishProgress(activity.getString(R.string.downloading_importing)+"-2")

        try {
            inputStream=conn?.getInputStream()
            if (inputStream!=null) {
                 result = importEvents(inputStream)
            }
            else
                activity.showErrorToast("no inputstream got")
        } catch(e: IOException) {
            Log.e(APP_TAG, "Couldn't read calendar", e)
            activity.showErrorToast(e, Toast.LENGTH_LONG)
        } catch(e: Exception) {
            Log.e(APP_TAG, "Couldn't process calendar", e)
            activity.showErrorToast(e, Toast.LENGTH_LONG)
        } finally {
            (conn as? HttpURLConnection)?.disconnect()
        }

        return result
    }

    private fun importEvents(inputStream: InputStream, defaultEventType: Int=0, calDAVCalendarId: Int=0): Boolean {
        try {
            activity.dbHelper.deleteImportedEvents()
            Log.d(APP_TAG,"import events deleted")
            var prevLine = ""

            inputStream.bufferedReader().use {
                while (true) {
                    var line = it.readLine() ?: break
                    if (line.trim().isEmpty()) {
                        continue
                    }

                    if (line.substring(0, 1) == " ") {
                        line = prevLine + line.trim()
                        eventsFailed--
                    }

                    if (isDescription) {
                        if (line.startsWith('\t')) {
                            curDescription += line.trimStart('\t').replace("\\n", "\n")
                        } else {
                            isDescription = false
                        }
                    }

                    if (line == BEGIN_EVENT) {
                        resetValues()
                        eventsTotal++
                        curEventType = defaultEventType
                    } else if (line.startsWith(DTSTART)) {
                        curStart = getTimestamp(line.substring(DTSTART.length))
                    } else if (line.startsWith(DTEND)) {
                        curEnd = getTimestamp(line.substring(DTEND.length))
                    } else if (line.startsWith(DURATION)) {
                        val duration = line.substring(DURATION.length)
                        curEnd = curStart + Parser().parseDurationSeconds(duration)
                    } else if (line.startsWith(SUMMARY) && !isNotificationDescription) {
                        curTitle = line.substring(SUMMARY.length)
                        curTitle = getTitle(curTitle).replace("\\n", "\n")
                    } else if (line.startsWith(DESCRIPTION) && !isNotificationDescription) {
                        curDescription = line.substring(DESCRIPTION.length).replace("\\n", "\n")
                        isDescription = true
                    } else if (line.startsWith(UID)) {
                        curImportId = line.substring(UID.length).trim()
                    } else if (line.startsWith(RRULE)) {
                        val repeatRule = Parser().parseRepeatInterval(line.substring(RRULE.length), curStart)
                        curRepeatRule = repeatRule.repeatRule
                        curRepeatInterval = repeatRule.repeatInterval
                        curRepeatLimit = repeatRule.repeatLimit
                    } else if (line.startsWith(ACTION)) {
                        isNotificationDescription = true
                        isProperReminderAction = line.substring(ACTION.length) == DISPLAY
                    } else if (line.startsWith(TRIGGER)) {
                        curReminderTriggerMinutes = Parser().parseDurationSeconds(line.substring(TRIGGER.length)) / 60
                    } else if (line.startsWith(CATEGORY_COLOR)) {
                        curCategoryColor = line.substring(CATEGORY_COLOR.length)
                    } else if (line.startsWith(CATEGORIES)) {
                        val categories = line.substring(CATEGORIES.length)
                        tryAddCategories(categories, activity)
                    } else if (line.startsWith(LAST_MODIFIED)) {
                        curLastModified = getTimestamp(line.substring(LAST_MODIFIED.length)) * 1000L
                    } else if (line.startsWith(EXDATE)) {
                        var value = line.substring(EXDATE.length)
                        if (value.endsWith('}')) {
                            value = value.substring(0, value.length - 1)
                        }

                        curRepeatExceptions.add(getTimestamp(value))
                    } else if (line.startsWith(LOCATION)) {
                        curLocation = line.substring(LOCATION.length)
                    } else if (line == END_ALARM) {
                        if (isProperReminderAction && curReminderTriggerMinutes != -1) {
                            curReminderMinutes.add(curReminderTriggerMinutes)
                        }
                    } else if (line.startsWith(SEQUENCE)){
                        continue
                    }else if (line == END_EVENT) {
                        if (curStart != -1 && curEnd == -1) {
                            curEnd = curStart
                        }

                        if (curTitle.isEmpty() || curStart == -1) {
                            continue
                        }

                        val event = Event(0, curStart, curEnd, curTitle, curDescription,importId = curImportId,color =  colorInt(curCategoryColor),source=SOURCE_IMPORTED_ICS)

                        if (event.getIsAllDay() && curEnd > curStart) {
                            event.endTS -= DAY_SECONDS
                        }

                        activity.dbHelper.insert(event, true) {
                            eventsImported++
                        }

                        resetValues()
                    }
                    prevLine = line
                }
            }
        } catch (e: Exception) {
            activity.showErrorToast(e, Toast.LENGTH_LONG)
            eventsFailed++
        }

        return when {
            (eventsImported < eventsTotal) -> false
            else -> true
        }
    }

    private fun getTimestamp(fullString: String): Int {
        return try {
            if (fullString.startsWith(';')) {
                val value = fullString.substring(fullString.lastIndexOf(':') + 1)
                if (!value.contains("T")) {
                    curFlags = curFlags or FLAG_ALL_DAY
                }

                Parser().parseDateTimeValue(value)
            } else {
                Parser().parseDateTimeValue(fullString.substring(1))
            }
        } catch (e: Exception) {
            activity.showErrorToast(e, Toast.LENGTH_LONG)
            eventsFailed++
            -1
        }
    }

    private fun tryAddCategories(categories: String, context: Context) {
        val eventTypeTitle = if (categories.contains(",")) {
            categories.split(",")[0]
        } else {
            categories
        }

        val eventId = context.dbHelper.getEventTypeIdWithTitle(eventTypeTitle)
        curEventType = if (eventId == -1) {
            val newTypeColor = if (curCategoryColor == "") context.resources.getColor(R.color.color_primary) else colorInt(curCategoryColor)
            val eventType = EventType(0, eventTypeTitle, newTypeColor)
            context.dbHelper.insertEventType(eventType)
        } else {
            eventId
        }
    }

    private fun getTitle(title: String): String {
        return if (title.startsWith(";") && title.contains(":")) {
            title.substring(title.lastIndexOf(':') + 1)
        } else {
            title.substring(1, Math.min(title.length, 80))
        }
    }

    private fun resetValues() {
        curStart = -1
        curEnd = -1
        curTitle = ""
        curDescription = ""
        curImportId = ""
        curFlags = 0
        curReminderMinutes = ArrayList()
        curRepeatExceptions = ArrayList()
        curRepeatInterval = 0
        curRepeatLimit = 0
        curRepeatRule = 0
        curEventType = DBHelper.REGULAR_EVENT_TYPE_ID
        curLastModified = 0L
        curCategoryColor = ""
        curLocation = ""
        isNotificationDescription = false
        isProperReminderAction = false
        curReminderTriggerMinutes = -1
    }

    private fun colorInt(colorString:String):Int {
        return when (colorString) {
            "red" -> (0xffff0000.toInt())
            "blue" ->(0xff0000ff.toInt())
            "gray" -> (0xff888888.toInt())
            else -> (0)
        }
    }

    private fun handleParseResult(result: IcsImporter.ImportResult) {
        if (result ==  IcsImporter.ImportResult.IMPORT_IGNORED) return
        activity.toast(when (result) {
            IcsImporter.ImportResult.IMPORT_OK -> R.string.import_successfully
            IcsImporter.ImportResult.IMPORT_PARTIAL -> R.string.import_some_failed
            else -> R.string.import_failed
        }, Toast.LENGTH_LONG)
    }

    override fun onPreExecute() {
        (activity as MainActivity).processProgressBar(SHOW_PROGRESSBAR)
    }

    override fun onProgressUpdate(vararg values: String?) {
        (activity as MainActivity).processProgressBar(UPDATE_PROGRESSBAR,values[0])
    }

    override fun onPostExecute(result: Boolean) {

        (activity as MainActivity).apply{
            if (result){
                toast(R.string.import_successfully,Toast.LENGTH_LONG)
                config.lastSuccessfulDataImportMilliSeconds = System.currentTimeMillis()
            }
            else{
                toast(R.string.import_failed,Toast.LENGTH_LONG)
                config.lastSuccessfulDataImportMilliSeconds = -1
            }
            refreshViewPager()
            processProgressBar(DISMISS_PROGRESSBAR)
        }
    }
}
