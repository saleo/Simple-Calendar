package com.simplemobiletools.calendar.services

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.simplemobiletools.calendar.extensions.processReminders
import com.simplemobiletools.calendar.helpers.*

class PostponeService : IntentService("Postpone") {
    override fun onHandleIntent(intent: Intent) {
        val ntfId = intent.getIntExtra(NOTIFICATION_ID, 0)
        val ntfTitle=intent.getStringExtra(NOTIFICATION_TITLE)
        val ntfContent=intent.getStringExtra(NOTIFICATION_CONTENT)
        val ntfTS=intent.getLongExtra(NOTIFICATION_TS,0)
        processReminders(eventIdsToProcess= ArrayList(0),notifyTS = ntfTS+ POSTPONE_TS,inNtfId = ntfId,inNtfTitle = ntfTitle,inNtfContent = ntfContent)
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(ntfId)
    }
}
