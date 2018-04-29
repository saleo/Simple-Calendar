/*
 * Copyright (c) Ricki Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.icsdroid.db

import android.content.ContentProviderOperation.Builder
import android.content.ContentValues
import android.provider.CalendarContract
import at.bitfire.ical4android.AndroidCalendar
import at.bitfire.ical4android.AndroidEvent
import at.bitfire.ical4android.AndroidEventFactory
import at.bitfire.ical4android.Event
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.property.LastModified

class LocalEvent: AndroidEvent {

    companion object {
        val COLUMN_LAST_MODIFIED = CalendarContract.Events.SYNC_DATA2
    }

    var uid: String? = null
    var lastModified = 0L

    private constructor(calendar: AndroidCalendar<AndroidEvent>, id: Long, baseInfo: ContentValues?): super(calendar, id, baseInfo) {
        uid = baseInfo?.getAsString(CalendarContract.Events._SYNC_ID)
        lastModified = baseInfo?.getAsLong(COLUMN_LAST_MODIFIED) ?: 0
    }

    constructor(calendar: AndroidCalendar<AndroidEvent>, event: Event): super(calendar, event) {
        uid = event.uid
        lastModified = event.lastModified?.dateTime?.time ?: 0
    }

    override fun populateEvent(values: ContentValues) {
        super.populateEvent(values)

        val event = requireNotNull(event)
        event.uid = values.getAsString(CalendarContract.Events._SYNC_ID)

        values.getAsLong(COLUMN_LAST_MODIFIED).let {
            lastModified = it
            event.lastModified = LastModified(DateTime(it))
        }
    }

    override fun buildEvent(recurrence: Event?, builder: Builder) {
        super.buildEvent(recurrence, builder)

        if (recurrence == null) {
            // master event
            builder .withValue(CalendarContract.Events._SYNC_ID, uid)
                    .withValue(COLUMN_LAST_MODIFIED, lastModified)
        } else
            // exception
            builder.withValue(CalendarContract.Events.ORIGINAL_SYNC_ID, uid)
    }


    object Factory: AndroidEventFactory<LocalEvent> {

        override fun newInstance(calendar: AndroidCalendar<AndroidEvent>, id: Long, baseInfo: ContentValues?) =
                LocalEvent(calendar, id, baseInfo)

        override fun newInstance(calendar: AndroidCalendar<AndroidEvent>, event: Event) =
                LocalEvent(calendar, event)

    }

}
