package net.euse.skcal.dialogs

import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.LinearLayout
import net.euse.skcal.extensions.dbHelper
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_export_events.view.*
import java.io.File
import java.util.*

class ExportEventsDialog(val activity: net.euse.skcal.activities.SimpleActivity, val path: String, val callback: (exportPastEvents: Boolean, file: File, eventTypes: HashSet<String>) -> Unit) {

    init {
        val view = (activity.layoutInflater.inflate(net.euse.skcal.R.layout.dialog_export_events, null) as ViewGroup).apply {
            export_events_folder.text = activity.humanizePath(path)
            export_events_filename.setText("events_${activity.getCurrentFormattedDateTime()}")

            activity.dbHelper.getEventTypes {
                val eventTypes = HashSet<String>()
                it.mapTo(eventTypes, { it.id.toString() })

                activity.runOnUiThread {
                    export_events_types_list.adapter = net.euse.skcal.adapters.FilterEventTypeAdapter(activity, it, eventTypes)
                    if (it.size > 1) {
                        export_events_pick_types.beVisible()

                        val margin = activity.resources.getDimension(net.euse.skcal.R.dimen.normal_margin).toInt()
                        (export_events_checkbox.layoutParams as LinearLayout.LayoutParams).leftMargin = margin
                    }
                }
            }
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(net.euse.skcal.R.string.ok, null)
                .setNegativeButton(net.euse.skcal.R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this, net.euse.skcal.R.string.export_events) {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val filename = view.export_events_filename.value
                    when {
                        filename.isEmpty() -> activity.toast(net.euse.skcal.R.string.empty_name)
                        filename.isAValidFilename() -> {
                            val file = File(path, "$filename.ics")
                            if (file.exists()) {
                                activity.toast(net.euse.skcal.R.string.name_taken)
                                return@setOnClickListener
                            }

                            val eventTypes = (view.export_events_types_list.adapter as net.euse.skcal.adapters.FilterEventTypeAdapter).getSelectedItemsSet()
                            callback(view.export_events_checkbox.isChecked, file, eventTypes)
                            dismiss()
                        }
                        else -> activity.toast(net.euse.skcal.R.string.invalid_name)
                    }
                }
            }
        }
    }
}
