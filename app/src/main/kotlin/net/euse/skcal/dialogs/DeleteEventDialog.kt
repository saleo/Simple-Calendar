package net.euse.skcal.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import net.euse.skcal.extensions.dbHelper
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_delete_event.view.*

class DeleteEventDialog(val activity: Activity, eventIds: List<Int>, val callback: (allOccurrences: Boolean) -> Unit) {
    val dialog: AlertDialog?

    init {
        val events = activity.dbHelper.getEventsWithIds(eventIds)
        val hasRepeatableEvent = events.any { it.repeatInterval > 0 }

        val view = activity.layoutInflater.inflate(net.euse.skcal.R.layout.dialog_delete_event, null).apply {
            delete_event_repeat_description.beVisibleIf(hasRepeatableEvent)
            delete_event_radio_view.beVisibleIf(hasRepeatableEvent)

            if (eventIds.size > 1) {
                delete_event_repeat_description.text = resources.getString(net.euse.skcal.R.string.selection_contains_repetition)
            }
        }

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(net.euse.skcal.R.string.yes, { dialog, which -> dialogConfirmed(view as ViewGroup, hasRepeatableEvent) })
                .setNegativeButton(net.euse.skcal.R.string.no, null)
                .create().apply {
            activity.setupDialogStuff(view, this)
        }
    }

    private fun dialogConfirmed(view: ViewGroup, hasRepeatableEvent: Boolean) {
        val deleteAllOccurrences = !hasRepeatableEvent || view.delete_event_radio_view.checkedRadioButtonId == net.euse.skcal.R.id.delete_event_all
        dialog?.dismiss()
        callback(deleteAllOccurrences)
    }
}
