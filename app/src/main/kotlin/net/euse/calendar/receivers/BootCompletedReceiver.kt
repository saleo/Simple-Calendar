package net.euse.calendar.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.euse.calendar.extensions.notifyRunningEvents
import net.euse.calendar.extensions.scheduleAllEvents

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, arg1: Intent) {
        context.apply {
            scheduleAllEvents()
            notifyRunningEvents()
            //recheckCalDAVCalendars {}
        }
    }
}
