package net.euse.calendar.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.euse.calendar.helpers.REPEAT_DOWNLOAD_IMPORT_FAIL
import net.euse.calendar.helpers.REPEAT_DOWNLOAD_IMPORT_RESULT
import net.euse.calendar.helpers.REPEAT_DOWNLOAD_IMPORT_SUCCESS
import net.euse.calendar.helpers.RepeatIcsImporter

class DownloadImportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var notifyIntent=Intent(context,NotificationReceiver::class.java)
        if (RepeatIcsImporter(context).download_import())
            notifyIntent.putExtra(REPEAT_DOWNLOAD_IMPORT_RESULT,REPEAT_DOWNLOAD_IMPORT_SUCCESS)
        else
            notifyIntent.putExtra(REPEAT_DOWNLOAD_IMPORT_RESULT, REPEAT_DOWNLOAD_IMPORT_FAIL)
        context.sendBroadcast(notifyIntent)
    }
}
