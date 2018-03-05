package net.euse.skcal.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import net.euse.skcal.extensions.dbHelper
import net.euse.skcal.interfaces.DeleteEventTypesListener
import net.euse.skcal.models.EventType
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.activity_manage_event_types.*
import java.util.*

class ManageEventTypesActivity : net.euse.skcal.activities.SimpleActivity(), DeleteEventTypesListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(net.euse.skcal.R.layout.activity_manage_event_types)

        getEventTypes()
        updateTextColors(manage_event_types_list)
    }

    private fun showEventTypeDialog(eventType: EventType? = null) {
        net.euse.skcal.dialogs.UpdateEventTypeDialog(this, eventType?.copy()) {
            getEventTypes()
        }
    }

    private fun getEventTypes() {
        dbHelper.getEventTypes {
            runOnUiThread {
                val adapter = net.euse.skcal.adapters.ManageEventTypesAdapter(this, it, this, manage_event_types_list) {
                    showEventTypeDialog(it as EventType)
                }
                adapter.setupDragListener(true)
                manage_event_types_list.adapter = adapter
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(net.euse.skcal.R.menu.menu_event_types, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            net.euse.skcal.R.id.add_event_type -> showEventTypeDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun deleteEventTypes(eventTypes: ArrayList<EventType>, deleteEvents: Boolean): Boolean {
        if (eventTypes.any { it.caldavCalendarId != 0 }) {
            toast(net.euse.skcal.R.string.unsync_caldav_calendar)
            if (eventTypes.size == 1) {
                return false
            }
        }

        dbHelper.deleteEventTypes(eventTypes, deleteEvents) {
            if (it == 0) {
                toast(net.euse.skcal.R.string.unknown_error_occurred)
            }
        }
        return true
    }
}
