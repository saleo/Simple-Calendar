package com.simplemobiletools.calendar.adapters

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.activities.SimpleActivity
import com.simplemobiletools.calendar.dialogs.DeleteEventDialog
import com.simplemobiletools.calendar.extensions.config
import com.simplemobiletools.calendar.extensions.dbHelper
import com.simplemobiletools.calendar.extensions.shareEvents
import com.simplemobiletools.calendar.helpers.Formatter
import com.simplemobiletools.calendar.models.Event
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.views.MyRecyclerView
import kotlinx.android.synthetic.main.customize_event_item_settings_view.view.*
import kotlinx.android.synthetic.main.event_item_day_view.view.*


class CustomizeEventsAdapter(activity: SimpleActivity, val events: ArrayList<Event>, recyclerView: MyRecyclerView,
                             itemClick: (Any) -> Unit,itemLongClick: (Any) -> Boolean)
    : MyRecyclerViewAdapter(activity, recyclerView, null, itemClick,itemLongClick) {

    override fun getActionMenuId() = 0

    override fun prepareActionMode(menu: Menu) {}

    override fun prepareItemSelection(view: View) {}

    override fun markItemSelection(select: Boolean, view: View?) {
        view?.event_item_frame?.isSelected = select
    }

    override fun actionItemPressed(id: Int) {
    }

    override fun getSelectableItemCount() = events.size

    override fun getItemViewType(position: Int): Int {
        if (itemCount<=0) return -1
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =(
        if (viewType!=-1)
            createViewHolder(R.layout.customize_event_item_settings_view, parent)
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
            tv_customize_event_item_title.apply {
                text = event.title
                setOnClickListener({itemClick(event)})
                setOnLongClickListener({
                    itemLongClick!!.invoke(event)
                })
            }

            tv_customize_event_item_lunar.apply {
                text = event.lunar
                setOnClickListener({itemClick(event)})
                setOnLongClickListener({
                    itemLongClick!!.invoke(event)
                })
            }

        }
    }
}
