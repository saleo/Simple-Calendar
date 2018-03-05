package net.euse.skcal.services

import android.content.Intent
import android.widget.RemoteViewsService

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = net.euse.skcal.adapters.EventListWidgetAdapter(applicationContext)
}
