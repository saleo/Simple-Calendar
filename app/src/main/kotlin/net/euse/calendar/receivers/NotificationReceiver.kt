package net.euse.calendar.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import net.euse.calendar.extensions.dbHelper
import net.euse.calendar.extensions.notifyDownloadImportResult
import net.euse.calendar.extensions.postGroupedNotify
import net.euse.calendar.extensions.updateListWidget
import net.euse.calendar.helpers.APP_TAG
import net.euse.calendar.helpers.REPEAT_DOWNLOAD_IMPORT_RESULT
import net.euse.calendar.helpers.REPEAT_DOWNLOAD_IMPORT_SUCCESS

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, APP_TAG)
        wakelock.acquire(5000)

        context.updateListWidget()
//        val id = intent.getIntExtra(EVENT_ID, -1)
//        if (id == -1) {
//            return
//        }
//
//        val event = context.dbHelper.getEventWithId(id)
//        if (event == null || event.getReminders().isEmpty()) {
//            return
//        }

        val downloadImportResult=intent.getStringExtra(REPEAT_DOWNLOAD_IMPORT_RESULT)
        if (downloadImportResult!=null) {
            if (downloadImportResult== REPEAT_DOWNLOAD_IMPORT_SUCCESS)
                context.notifyDownloadImportResult(true)
            else
                context.notifyDownloadImportResult(false)
        }else{
            val gn=context.dbHelper.getDayGroupedNotification()
            gn.apply {
                if (ntfId!=-1)
                    context.postGroupedNotify(ntfId,ntfTitle,ntfContent,ntfTms)
            }
        }
//        if (!event.ignoreEventOccurrences.contains(Formatter.getDayCodeFromTS(event.startTS).toInt())) {
//            context.postGroupedNotify(event,myId,myTitle,myContent)
//        }
//        context.scheduleAllEvents()
    }
}
