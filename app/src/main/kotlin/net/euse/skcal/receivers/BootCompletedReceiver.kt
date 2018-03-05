package net.euse.skcal.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.euse.skcal.extensions.notifyRunningEvents
import net.euse.skcal.extensions.recheckCalDAVCalendars
import net.euse.skcal.extensions.scheduleAllEvents

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, arg1: Intent) {
        context.apply {
            scheduleAllEvents()
            notifyRunningEvents()
            recheckCalDAVCalendars {}
        }
    }
}
