package net.euse.skcal.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import net.euse.skcal.extensions.config
import net.euse.skcal.extensions.dbHelper
import net.euse.skcal.extensions.rescheduleReminder
import net.euse.skcal.extensions.showEventReminderDialog
import net.euse.skcal.helpers.EVENT_ID

class SnoozeReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showEventReminderDialog(config.snoozeDelay, true, cancelCallback = { dialogCancelled() }) {
            val eventId = intent.getIntExtra(EVENT_ID, 0)
            val event = dbHelper.getEventWithId(eventId)
            config.snoozeDelay = it
            rescheduleReminder(event, it)
            finishActivity()
        }
    }

    private fun dialogCancelled() {
        finishActivity()
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(0, 0)
    }
}
