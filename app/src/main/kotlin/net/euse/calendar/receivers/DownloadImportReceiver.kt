package net.euse.calendar.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import net.euse.calendar.extensions.dbHelper
import net.euse.calendar.helpers.*
import net.euse.calendar.models.Event
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadImportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var downloadImportResult=false

        val pendingResult=goAsync()

        class RepeatIcsImporter(val context: Context): AsyncTask<Void, String, Boolean>() {

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
                var url= URL(SKCAL_URL)
                var conn = url.openConnection()
                var inputStream: InputStream?

                var followRedirect = false
                var redirect = 0
                var result=false

                do {
                    try {
                        if (conn is HttpURLConnection) {
                            conn.setRequestProperty("User-Agent", USER_AGENT)
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
                                return false
                            }
                        } else
                        // local file, always simulate HTTP status 200 OK
                            requireNotNull(conn)

                    } catch(e: IOException) {
                        Log.e(APP_TAG, "Couldn't fetch calendar", e)
                    }
                    redirect++
                } while (followRedirect && redirect < MAX_REDIRECTS)

                try {
                    inputStream=conn?.getInputStream()
                    if (inputStream!=null) {
                        result = importEvents(inputStream)
                    }
                } catch(e: IOException) {
                    Log.e(APP_TAG, "Couldn't read calendar", e)
                } catch(e: Exception) {
                    Log.e(APP_TAG, "Couldn't process calendar", e)
                } finally {
                    (conn as? HttpURLConnection)?.disconnect()
                }

                pendingResult.finish()

                return result
            }

            private fun importEvents(inputStream: InputStream, defaultEventType: Int=0, calDAVCalendarId: Int=0): Boolean {
                try {
                    context.dbHelper.deleteImportedEvents()
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

                                val event = Event(0, curStart, curEnd, curTitle, curDescription,importId = curImportId,color =  colorInt(curCategoryColor),source= SOURCE_IMPORTED_ICS)

                                if (event.getIsAllDay() && curEnd > curStart) {
                                    event.endTS -= DAY_SECONDS
                                }

                                context.dbHelper.insert(event, true) {
                                    eventsImported++
                                }

                                resetValues()
                            }
                            prevLine = line
                        }
                    }
                } catch (e: Exception) {
                    eventsFailed++
                }

                if (eventsImported < eventsTotal)
                    return false
                else
                    return true
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
                    eventsFailed++
                    -1
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

            override fun onPostExecute(result: Boolean) {
                downloadImportResult=result
            }
        }//repeatIcsImporter class end

        RepeatIcsImporter(context).execute()

        var notifyIntent=Intent(context,NotificationReceiver::class.java)

        if (downloadImportResult)
            notifyIntent.putExtra(REPEAT_DOWNLOAD_IMPORT_RESULT,REPEAT_DOWNLOAD_IMPORT_SUCCESS)
        else
            notifyIntent.putExtra(REPEAT_DOWNLOAD_IMPORT_RESULT, REPEAT_DOWNLOAD_IMPORT_FAIL)
        context.sendBroadcast(notifyIntent)
    }
}
