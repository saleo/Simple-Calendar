package net.euse.skcal.dialogs

import android.support.v7.app.AlertDialog
import net.euse.skcal.extensions.config
import net.euse.skcal.extensions.dbHelper
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_filter_event_types.view.*

class FilterEventTypesDialog(val activity: net.euse.skcal.activities.SimpleActivity, val callback: () -> Unit) {
    var dialog: AlertDialog
    val view = activity.layoutInflater.inflate(net.euse.skcal.R.layout.dialog_filter_event_types, null)

    init {
        val eventTypes = activity.dbHelper.fetchEventTypes()
        val displayEventTypes = activity.config.displayEventTypes
        view.filter_event_types_list.adapter = net.euse.skcal.adapters.FilterEventTypeAdapter(activity, eventTypes, displayEventTypes)

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(net.euse.skcal.R.string.ok, { dialogInterface, i -> confirmEventTypes() })
                .setNegativeButton(net.euse.skcal.R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this, net.euse.skcal.R.string.filter_events_by_type)
        }
    }

    private fun confirmEventTypes() {
        val selectedItems = (view.filter_event_types_list.adapter as net.euse.skcal.adapters.FilterEventTypeAdapter).getSelectedItemsSet()
        if (activity.config.displayEventTypes != selectedItems) {
            activity.config.displayEventTypes = selectedItems
            callback()
        }
        dialog.dismiss()
    }
}
