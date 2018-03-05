package net.euse.skcal.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.euse.skcal.extensions.recheckCalDAVCalendars

class CalDAVSyncReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.recheckCalDAVCalendars {}
    }
}
