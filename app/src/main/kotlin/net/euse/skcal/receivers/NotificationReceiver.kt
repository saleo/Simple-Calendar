package net.euse.skcal.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import net.euse.skcal.extensions.dbHelper
import net.euse.skcal.extensions.notifyEvent
import net.euse.skcal.extensions.scheduleAllEvents
import net.euse.skcal.extensions.updateListWidget
import net.euse.skcal.helpers.EVENT_ID
import net.euse.skcal.helpers.Formatter

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Simple Calendar")
        wakelock.acquire(5000)

        context.updateListWidget()
        val id = intent.getIntExtra(EVENT_ID, -1)
        if (id == -1) {
            return
        }

        val event = context.dbHelper.getEventWithId(id)
        if (event == null || event.getReminders().isEmpty()) {
            return
        }

        if (!event.ignoreEventOccurrences.contains(Formatter.getDayCodeFromTS(event.startTS).toInt())) {
            context.notifyEvent(event)
        }
        context.scheduleAllEvents()
    }
}
