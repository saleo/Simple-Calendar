package com.simplemobiletools.calendar.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.opengl.Visibility
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import com.simplemobiletools.calendar.BuildConfig
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.dialogs.CustomEventReminderDialog
import com.simplemobiletools.calendar.dialogs.CustomEventRepeatIntervalDialog
import com.simplemobiletools.calendar.helpers.*
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.models.RadioItem
import kotlinx.android.synthetic.main.bottom_buttonbar.*
import kotlinx.android.synthetic.main.bottom_buttonbar.view.*
import kotlinx.android.synthetic.main.fragment_day.*
import kotlinx.android.synthetic.main.fragment_day.view.*
import kotlinx.android.synthetic.main.fragment_month.*
import kotlinx.android.synthetic.main.fragment_month.view.*
import java.io.File
import java.util.TreeSet
import kotlin.collections.ArrayList


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

fun Activity.setupBottomButtonBar(mHolder: ViewGroup) {
    mHolder.apply {
        ib_bcc_info.setOnClickListener {
            context.launchAbout()
        }

        ib_bcc_setting.setOnClickListener {
            context.launchSettings()
        }

        ib_bcc_today.setOnClickListener {
            context.gotoToday()
        }

        ib_bcc_share.setOnClickListener {
            shareScreen()
        }

        ib_bcc_recommend.setOnClickListener {
            val appName = getString(R.string.app_name)
            val text = String.format(getString(R.string.share_text), appName, getString(R.string.my_website))
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, appName)
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                startActivity(Intent.createChooser(this, getString(R.string.invite_via)))
            }

        }
    }

    if (mHolder as RelativeLayout == rl_day_holder || mHolder == rl_monthcalendar_holder){
        ib_bcc_plus.visibility= View.VISIBLE
        ib_bcc_plus.setOnClickListener { launchNewEventIntent() }
    }

}


fun Activity.captureWithStatusBar(): Bitmap {

    val view = window.decorView
    view.isDrawingCacheEnabled = true
    view.buildDrawingCache()
    val bmp = view.drawingCache
    val wm: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = Point(0, 0)
    wm.defaultDisplay.getSize(p)
    val width = p.x
    val height = p.y
    val ret = Bitmap.createBitmap(bmp, 0, 0, width, height)
    view.destroyDrawingCache()
    return ret
}

fun Activity.shareScreen(){

    val saveFile= File(externalCacheDir, "share.png")
    val saveFileItem= FileDirItem(saveFile.absolutePath, saveFile.name)
    val bitmap: Bitmap =captureWithStatusBar()

    try {
        getFileOutputStream(saveFileItem,true){
            if (it == null){
                showErrorToast("outputStream is null")
                return@getFileOutputStream
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.flush()
            it.close()
            sharePathIntent(saveFile.path, BuildConfig.APPLICATION_ID)
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
        showErrorToast(e)
    }
}
