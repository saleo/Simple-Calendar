package com.simplemobiletools.calendar.services

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.simplemobiletools.calendar.extensions.processEventRemindersNotification
import com.simplemobiletools.calendar.helpers.*

class PostponeService : IntentService("Postpone") {
    override fun onHandleIntent(intent: Intent) {
        val ntfId = intent.getIntExtra(NOTIFICATION_ID, 0)
        val ntfTitle=intent.getStringExtra(NOTIFICATION_TITLE)
        val ntfContent=intent.getStringExtra(NOTIFICATION_CONTENT)
        val ntfTS=intent.getLongExtra(NOTIFICATION_TS,0)
        //maybe need NOT check reminderSwich=true, since if can come here,the switch must be true state
        processEventRemindersNotification(eventIdsToProcess= ArrayList(0),notifyTms = ntfTS+ POSTPONE_TS,inNtfId = ntfId,inNtfTitle = ntfTitle,inNtfContent = ntfContent)
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(ntfId)
    }
}
