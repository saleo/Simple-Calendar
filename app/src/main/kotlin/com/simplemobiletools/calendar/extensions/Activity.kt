package com.simplemobiletools.calendar.extensions

import android.app.Activity
import android.content.Context
import com.simplemobiletools.calendar.BuildConfig
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.dialogs.CustomEventReminderDialog
import com.simplemobiletools.calendar.dialogs.CustomEventRepeatIntervalDialog
import com.simplemobiletools.calendar.helpers.*
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.getFormattedMinutes
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.extensions.sharePathIntent
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.models.RadioItem
import java.io.File
import java.util.TreeSet
import kotlin.collections.ArrayList
import android.graphics.Bitmap
import android.graphics.Point
import android.view.WindowManager


fun BaseSimpleActivity.shareEvents(ids: List<Int>) {
    val file = getTempFile()
    if (file == null) {
        toast(R.string.unknown_error_occurred)
        return
    }

    val events = dbHelper.getEventsWithIds(ids)
    IcsExporter().exportEvents(this, file, events) {
        if (it == IcsExporter.ExportResult.EXPORT_OK) {
            sharePathIntent(file.absolutePath, BuildConfig.APPLICATION_ID)
        }
    }
}

fun BaseSimpleActivity.getTempFile(): File? {
    val folder = File(cacheDir, "events")
    if (!folder.exists()) {
        if (!folder.mkdir()) {
            toast(R.string.unknown_error_occurred)
            return null
        }
    }

    return File(folder, "events.ics")
}

    fun Activity.showEventReminderDialog(curMinutes: Int, isSnoozePicker: Boolean = false, cancelCallback: (() -> Unit)? = null, callback: (minutes: Int) -> Unit) {
    hideKeyboard()
    val minutes = TreeSet<Int>()
    minutes.apply {
        if (!isSnoozePicker) {
            //for settings-activity already have one equal reminders_switch
//            add(-1)
            add(0)
        }
        add(5)
        add(10)
        add(20)
        add(30)
        add(60)
        add(120)
        add(180)
        add(curMinutes)
    }

    val items = ArrayList<RadioItem>(minutes.size + 1)
    minutes.mapIndexedTo(items, { index, value ->
        RadioItem(index, getFormattedMinutes(value, !isSnoozePicker), value)
    })

    var selectedIndex = 0
    minutes.forEachIndexed { index, value ->
        if (value == curMinutes) {
            selectedIndex = index
        }
    }

    items.add(RadioItem(-2, getString(R.string.custom)))

    RadioGroupDialog(this, items, selectedIndex, showOKButton = isSnoozePicker, cancelCallback = cancelCallback) {
        if (it == -2) {
            CustomEventReminderDialog(this) {
                callback(it)
            }
        } else {
            callback(it as Int)
        }
    }
}

fun Activity.showEventRepeatIntervalDialog(curSeconds: Int, callback: (minutes: Int) -> Unit) {
    hideKeyboard()
    val seconds = TreeSet<Int>()
    seconds.apply {
        add(0)
        add(DAY)
        add(WEEK)
        add(MONTH)
        add(YEAR)
        add(curSeconds)
    }

    val items = ArrayList<RadioItem>(seconds.size + 1)
    seconds.mapIndexedTo(items, { index, value ->
        RadioItem(index, getRepetitionText(value), value)
    })

    var selectedIndex = 0
    seconds.forEachIndexed { index, value ->
        if (value == curSeconds)
            selectedIndex = index
    }

    items.add(RadioItem(-1, getString(R.string.custom)))

    RadioGroupDialog(this, items, selectedIndex) {
        if (it == -1) {
            CustomEventRepeatIntervalDialog(this) {
                callback(it)
            }
        } else {
            callback(it as Int)
        }
    }

}

/**
 * 获取当前屏幕截图，包含状态栏
 *
 * @param activity activity
 * @return Bitmap
 */
fun Activity.captureWithStatusBar(context: Context): Bitmap {
    val view = this.window.decorView
    view.isDrawingCacheEnabled = true
    view.buildDrawingCache()
    val bmp = view.drawingCache
    val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = Point(0,0)
    wm.defaultDisplay.getSize(p)
    val width = p.x
    val height = p.y
    val ret = Bitmap.createBitmap(bmp, 0, 0, width, height)
    view.destroyDrawingCache()
    return ret
}
