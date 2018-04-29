/*
 * Copyright (c) Ricki Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.icsdroid.db

import android.accounts.Account
import android.content.ContentProviderClient
import android.content.ContentUris
import android.content.ContentValues
import android.database.DatabaseUtils
import android.os.RemoteException
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.Events
import at.bitfire.ical4android.AndroidCalendar
import at.bitfire.ical4android.AndroidCalendarFactory
import at.bitfire.ical4android.CalendarStorageException
import java.io.FileNotFoundException

class LocalCalendar private constructor(
        account: Account,
        provider: ContentProviderClient,
        id: Long
): AndroidCalendar<LocalEvent>(account, provider, LocalEvent.Factory, id) {

    companion object {

        val DEFAULT_COLOR = 0xFF2F80C7.toInt()

        val COLUMN_ETAG = Calendars.CAL_SYNC1
        val COLUMN_LAST_MODIFIED = Calendars.CAL_SYNC4
        val COLUMN_LAST_SYNC = Calendars.CAL_SYNC5
        val COLUMN_ERROR_MESSAGE = Calendars.CAL_SYNC6

        @Deprecated("for compatibility only (read-only); see CalendarCredentials instead")
        val COLUMN_USERNAME = Calendars.CAL_SYNC2
        @Deprecated("for compatibility only (read-only); see CalendarCredentials instead")
        val COLUMN_PASSWORD = Calendars.CAL_SYNC3

        @Throws(FileNotFoundException::class, CalendarStorageException::class)
        fun findById(account: Account, provider: ContentProviderClient, id: Long) =
                AndroidCalendar.findByID(account, provider, Factory, id)

        @Throws(CalendarStorageException::class)
        fun findAll(account: Account, provider: ContentProviderClient) =
                AndroidCalendar.find(account, provider, Factory, null, null)

    }

    var url: String? = null             // URL of iCalendar file
    var eTag: String? = null            // iCalendar ETag at last successful sync

    @Deprecated("for compatibility only (read-only); see CalendarCredentials instead")
    var legacyUsername: String? = null        // HTTP username (or null if no auth. required)
    @Deprecated("for compatibility only (read-only); see CalendarCredentials instead")
    var legacyPassword: String? = null        // HTTP password (or null if no auth. required)

    var lastModified = 0L               // iCalendar Last-Modified at last successful sync (or 0 for none)
    var lastSync = 0L                   // time of last sync (0 if none)
    var errorMessage: String? = null    // error message (HTTP status or exception name) of last sync (or null)


    override fun eventBaseInfoColumns() =
            arrayOf(CalendarContract.Events._ID, CalendarContract.Events._SYNC_ID, LocalEvent.COLUMN_LAST_MODIFIED)


    override fun populate(info: ContentValues) {
        super.populate(info)
        url = info.getAsString(Calendars.NAME)

        legacyUsername = info.getAsString(COLUMN_USERNAME)
        legacyPassword = info.getAsString(COLUMN_PASSWORD)

        eTag = info.getAsString(COLUMN_ETAG)
        info.getAsLong(COLUMN_LAST_MODIFIED)?.let { lastModified = it }

        info.getAsLong(COLUMN_LAST_SYNC)?.let { lastSync = it }
        errorMessage = info.getAsString(COLUMN_ERROR_MESSAGE)
    }

    @Throws(CalendarStorageException::class)
    fun updateStatusSuccess(eTag: String?, lastModified: Long) {
        this.eTag = eTag
        this.lastModified = lastModified
        lastSync = System.currentTimeMillis()

        val values = ContentValues(4)
        values.put(COLUMN_ETAG, eTag)
        values.put(COLUMN_LAST_MODIFIED, lastModified)
        values.put(COLUMN_LAST_SYNC, lastSync)
        values.putNull(COLUMN_ERROR_MESSAGE)
        update(values)
    }

    @Throws(CalendarStorageException::class)
    fun updateStatusNotModified() {
        lastSync = System.currentTimeMillis()

        val values = ContentValues(1)
        values.put(COLUMN_LAST_SYNC, lastSync)
        update(values)
    }

    @Throws(CalendarStorageException::class)
    fun updateStatusError(message: String) {
        eTag = null
        lastModified = 0
        lastSync = System.currentTimeMillis()
        errorMessage = message

        val values = ContentValues(4)
        values.putNull(COLUMN_ETAG)
        values.putNull(COLUMN_LAST_MODIFIED)
        values.put(COLUMN_LAST_SYNC, lastSync)
        values.put(COLUMN_ERROR_MESSAGE, message)
        update(values)
    }

    @Throws(CalendarStorageException::class)
    fun updateUrl(url: String) {
        this.url = url

        val values = ContentValues(1)
        values.put(Calendars.NAME, url)
        update(values)
    }

    @Throws(CalendarStorageException::class)
    fun queryByUID(uid: String) =
            queryEvents("${Events._SYNC_ID}=?", arrayOf(uid))

    @Throws(CalendarStorageException::class)
    fun retainByUID(uids: Set<String>): Int {
        var deleted = 0
        try {
            provider.query(syncAdapterURI(Events.CONTENT_URI, account),
                    arrayOf(Events._ID, Events._SYNC_ID, Events.ORIGINAL_SYNC_ID),
                    "${Events.CALENDAR_ID}=?", arrayOf(id.toString()), null).use { row ->
                while (row.moveToNext()) {
                    val eventId = row.getLong(0)
                    val syncId = row.getString(1)
                    val originalSyncId = row.getString(2)
                    if (!uids.contains(syncId) && !uids.contains(originalSyncId)) {
                        provider.delete(syncAdapterURI(ContentUris.withAppendedId(Events.CONTENT_URI, eventId), account), null, null)
                        deleted++
                    }
                }
            }
            return deleted
        } catch(e: RemoteException) {
            throw CalendarStorageException("Couldn't delete local events")
        }
    }


    object Factory: AndroidCalendarFactory<LocalCalendar> {

        override fun newInstance(account: Account, provider: ContentProviderClient, id: Long) =
                LocalCalendar(account, provider, id)

    }

}
