package net.euse.calendar.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import net.euse.calendar.extensions.config
import net.euse.calendar.extensions.scheduleEventsReminder
import net.euse.calendar.helpers.APP_TAG
import net.euse.calendar.helpers.SCHEDULE_ACTIVE

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, arg1: Intent) {
        context.apply {
            if (context.config.reminderSwitch)
                scheduleEventsReminder(SCHEDULE_ACTIVE)
            //scheduleDownloadImport(true)
            //recheckCalDAVCalendars {}
        }
    }
}
