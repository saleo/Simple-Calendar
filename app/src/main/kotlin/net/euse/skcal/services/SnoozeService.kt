package net.euse.skcal.services

import android.app.IntentService
import android.content.Intent
import net.euse.skcal.extensions.config
import net.euse.skcal.extensions.dbHelper
import net.euse.skcal.extensions.rescheduleReminder
import net.euse.skcal.helpers.EVENT_ID

class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent) {
        val eventId = intent.getIntExtra(EVENT_ID, 0)
        val event = dbHelper.getEventWithId(eventId)
        rescheduleReminder(event, config.snoozeDelay)
    }
}
