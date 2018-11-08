package com.simplemobiletools.calendar.adapters

import android.graphics.Bitmap
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.calendar.BuildConfig
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.SimpleActivity
import com.simplemobiletools.calendar.dialogs.DeleteEventDialog
import com.simplemobiletools.calendar.extensions.captureWithStatusBar
import com.simplemobiletools.calendar.extensions.config
import com.simplemobiletools.calendar.extensions.dbHelper
import com.simplemobiletools.calendar.extensions.shareEvents
import com.simplemobiletools.calendar.helpers.Formatter
import com.simplemobiletools.calendar.models.Event
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.getFileOutputStream
import com.simplemobiletools.commons.extensions.sharePathIntent
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.views.MyRecyclerView
import kotlinx.android.synthetic.main.event_item_day_view.view.*
import java.io.File


class DayEventsAdapter(activity: SimpleActivity, val events: ArrayList<Event>, recyclerView: MyRecyclerView, itemClick: (Any) -> Unit)
    : MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    override fun getActionMenuId() = R.menu.cab_day

    override fun prepareActionMode(menu: Menu) {}

    override fun prepareItemSelection(view: View) {}

    override fun markItemSelection(select: Boolean, view: View?) {
        view?.event_item_frame?.isSelected = select
    }

    override fun actionItemPressed(id: Int) {
        when (id) {
            R.id.cab_share -> shareEvents()
            R.id.cab_delete -> askConfirmDelete()
        }
    }

    override fun getSelectableItemCount() = events.size

    override fun getItemViewType(position: Int): Int {
        if (itemCount<=0) return -1;
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =(
        if (viewType!=-1)
            createViewHolder(R.layout.event_item_day_view, parent)
        else
            createViewHolder(R.layout.empty_recycler_view, parent)
        )

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val event = events[position]
        val view = holder.bindView(event) { itemView, layoutPosition ->
            setupView(itemView, event)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = events.size

    private fun setupView(view: View, event: Event) {
        view.apply {
            event_section_title.text = event.title
            event_reminder_time.text = Formatter.getTimeFromTS(context,event.startTS-context.config.currentReminderMinutes*60)
            event_item_shareto.applyColorFilter(textColor)

            event_reminder_time.setTextColor(textColor)
            event_section_title.setTextColor(event.color)

            setOnClickListener {
                shareEventTitle(event_section_title.text.toString())
            }
        }
    }

    private fun shareEvents() {
        val eventIds = ArrayList<Int>(selectedPositions.size)
        selectedPositions.forEach {
            eventIds.add(events[it].id)
        }
        activity.shareEvents(eventIds.distinct())
    }

    private fun askConfirmDelete() {
        val eventIds = ArrayList<Int>(selectedPositions.size)
        val timestamps = ArrayList<Int>(selectedPositions.size)
        selectedPositions.forEach {
            eventIds.add(events[it].id)
            timestamps.add(events[it].startTS)
        }

        DeleteEventDialog(activity, eventIds) {
            val eventsToDelete = ArrayList<Event>(selectedPositions.size)
            selectedPositions.sortedDescending().forEach {
                eventsToDelete.add(events[it])
            }
            events.removeAll(eventsToDelete)

            if (it) {
                val eventIDs = Array(eventIds.size, { i -> (eventIds[i].toString()) })
                activity.dbHelper.deleteEvents(eventIDs, true)
            } else {
                eventIds.forEachIndexed { index, value ->
                    activity.dbHelper.addEventRepeatException(value, timestamps[index], true)
                }
            }
            removeSelectedItems()
        }
    }

    private fun shareEventTitle(title:String){
        val saveFile=File(activity.externalCacheDir,"share.png")
        val saveFileItem=FileDirItem(saveFile.absolutePath,saveFile.name)
        val bitmap:Bitmap=activity.captureWithStatusBar(activity.applicationContext)

        try {
            activity.getFileOutputStream(saveFileItem,true){
                if (it == null){
                    activity.showErrorToast("outputStream is null")
                    return@getFileOutputStream
                }

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it);
                it.flush()
                it.close();
                activity.sharePathIntent(saveFile.path,BuildConfig.APPLICATION_ID)
            }

//            activity.startActivity(
//                    Intent().apply {
//                        action = Intent.ACTION_SEND
//                        putExtra(Intent.EXTRA_STREAM,saveFileUri)
//                        type = "*/*"
//                        createChooser(this, activity.applicationContext.getString(R.string.invite_via))
//                    }
//           )
        } catch (e: Exception) {
            activity.showErrorToast(e)
        }
    }

}
