package net.euse.calendar.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import net.euse.calendar.helpers.APP_TAG

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, arg1: Intent) {
Log.e(APP_TAG,"111")
        context.apply {
Log.e(APP_TAG,"222")
            //scheduleDownloadImport(true)
            //recheckCalDAVCalendars {}
        }
    }
}
