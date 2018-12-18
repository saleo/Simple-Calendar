package net.euse.calendar.services

import android.app.IntentService
import android.content.Intent
import net.euse.calendar.extensions.config
import net.euse.calendar.extensions.dbHelper
import net.euse.calendar.extensions.rescheduleReminder
import net.euse.calendar.helpers.EVENT_ID

class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent) {
        val eventId = intent.getIntExtra(EVENT_ID, 0)
        val event = dbHelper.getEventWithId(eventId)
        rescheduleReminder(event, config.snoozeDelay)
    }
}
