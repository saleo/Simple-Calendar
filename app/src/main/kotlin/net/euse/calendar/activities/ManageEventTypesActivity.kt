package net.euse.calendar.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import net.euse.calendar.R
import net.euse.calendar.adapters.ManageEventTypesAdapter
import net.euse.calendar.dialogs.UpdateEventTypeDialog
import net.euse.calendar.extensions.dbHelper
import net.euse.calendar.interfaces.DeleteEventTypesListener
import net.euse.calendar.models.EventType
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.activity_manage_event_types.*
import java.util.*

class ManageEventTypesActivity : SimpleActivity(), DeleteEventTypesListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_event_types)

        getEventTypes()
        updateTextColors(manage_event_types_list)
    }

    private fun showEventTypeDialog(eventType: EventType? = null) {
        UpdateEventTypeDialog(this, eventType?.copy()) {
            getEventTypes()
        }
    }

    private fun getEventTypes() {
        dbHelper.getEventTypes {
            runOnUiThread {
                val adapter = ManageEventTypesAdapter(this, it, this, manage_event_types_list) {
                    showEventTypeDialog(it as EventType)
                }
                adapter.setupDragListener(true)
                manage_event_types_list.adapter = adapter
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_event_types, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_event_type -> showEventTypeDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun deleteEventTypes(eventTypes: ArrayList<EventType>, deleteEvents: Boolean): Boolean {
        if (eventTypes.any { it.caldavCalendarId != 0 }) {
            toast(R.string.unsync_caldav_calendar)
            if (eventTypes.size == 1) {
                return false
            }
        }

        dbHelper.deleteEventTypes(eventTypes, deleteEvents) {
            if (it == 0) {
                toast(R.string.unknown_error_occurred)
            }
        }
        return true
    }
}
