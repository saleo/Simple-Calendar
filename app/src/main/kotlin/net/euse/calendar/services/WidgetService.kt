package net.euse.calendar.services

import android.content.Intent
import android.widget.RemoteViewsService
import net.euse.calendar.adapters.EventListWidgetAdapter

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = EventListWidgetAdapter(applicationContext)
}
