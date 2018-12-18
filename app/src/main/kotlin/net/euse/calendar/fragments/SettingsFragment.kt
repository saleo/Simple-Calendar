package net.euse.calendar.fragments

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import cn.carbs.android.gregorianlunarcalendar.library.data.ChineseCalendar
import net.euse.calendar.R
import net.euse.calendar.activities.MainActivity
import net.euse.calendar.adapters.CustomizeEventsAdapter
import net.euse.calendar.dialogs.CustomizeEventDialog
import net.euse.calendar.dialogs.CustomizeLunarDialog
import net.euse.calendar.extensions.*
import net.euse.calendar.helpers.*
import net.euse.calendar.helpers.Formatter
import net.euse.calendar.models.Event
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.fragment_settings.rv_customize_events1 as rv_customize_event
import kotlinx.android.synthetic.main.fragment_settings.*
import org.joda.time.DateTime
import java.util.*

class SettingsFragment: MyFragmentHolder(), AdapterView.OnItemSelectedListener,View.OnClickListener {
    private val GET_RINGTONE_URI = 1
    private val COL_ID = "id"
    private val COL_START_TS = "start_ts"
    private val COL_TITLE = "title"
    private val COL_LUNAR = "lunar"
    private val COL_SOURCE = "source"
    private val COL_WHOMFOR = "whomfor"
    private val COL_WHATFOR = "whatfor"
    private val COL_PARENT_EVENT_ID = "event_parent_id"

    lateinit var res: Resources
    private var mStoredPrimaryColor = 0
    private var mReminderMinutes = 0

    private var mWhomFor=""
    private var mWhatFor=""
    private var lastHash=0

    // These are the Contacts rows that we will retrieve
    val PROJECTION = arrayOf(COL_ID, "substr($COL_TITLE,1,instr($COL_TITLE,' ')-1) as whomfor",
            "substr($COL_TITLE,instr($COL_TITLE,' ')+1) as whatfor", COL_LUNAR)


    // This is the select criteria
    val SELECTION_CUSTOMIZED_EVENT_ORIGIN = "$COL_SOURCE=${SOURCE_CUSTOMIZE_ANNIVERSARY} and $COL_PARENT_EVENT_ID=0"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_settings,container,false)
        view.background = ColorDrawable(activity!!.config.backgroundColor)
        return view
    }

    override fun onResume() {
        super.onResume()

        val placeholderText = String.format(getString(R.string.string_placeholder), "${getString(R.string.no_data_in_customizeEvents)}\n")
        tv_settings_placeholder.text = placeholderText

        tv_settings_customize_event_when.text= PLACEHOLDER_8WHITESPACE
        tv_settings_customize_event_when.underlineText()

        res = resources

        mStoredPrimaryColor = activity!!.config.primaryColor        
        currentDayCode= Formatter.getTodayCode(context!!)
        (activity as MainActivity).updateTopBottom(view = ABOUT_CREDIT_VIEW)


        activity!!.updateTextColors(ll_settings_holder)
        checkPrimaryColor()
        setupReminerGeneral()
        setupCustomizeEvent()        
    }

    private fun checkPrimaryColor() {
        if (activity!!.config.primaryColor != mStoredPrimaryColor) {
            activity!!.dbHelper.getEventTypes {
                if (it.filter { it.caldavCalendarId == 0 }.size == 1) {
                    val eventType = it.first { it.caldavCalendarId == 0 }
                    eventType.color = activity!!.config.primaryColor
                    activity!!.dbHelper.updateEventType(eventType)
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent){
            acs_customize_event_whomfor -> {mWhomFor= parent?.getItemAtPosition(position).toString()}
            acs_customize_event_whatfor -> {mWhatFor= parent?.getItemAtPosition(position).toString()}
            acs_reminderTs -> { updateReminerTs(position)}
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onClick(v: View?) {//for several coomponent:add button, when textview
        if (v == btn_customize_event_add ){
            if (mWhomFor.isEmpty()){
                activity!!.toast(R.string.whomfor_empty)
                acs_customize_event_whomfor.requestFocus()
                return
            }
            if (mWhatFor.isEmpty()){
                activity!!.toast(R.string.whatfor_empty)
                acs_customize_event_whatfor.requestFocus()
                return
            }
            if (tv_settings_customize_event_when.value.isEmpty()){
                activity!!.toast(R.string.when_empty)
                tv_settings_customize_event_when.requestFocus()
                return
            }

            val title=mWhomFor+" "+mWhatFor
            activity!!.dbHelper.getEventsWithSearchQuery(title) exithere@{searchedText, events ->
                if (!events.isEmpty()) {
                    var events1=events.filter { event -> event.lunar==tv_settings_customize_event_when.value }
                    if (!events1.isEmpty()) {
                        activity!!.toast(R.string.customized_event_already_exist)
                        acs_customize_event_whomfor.requestFocus()
                        return@exithere
                    }

                    // lunar and when differs
                    events1=events.sortedBy { event -> event.id  }
                    ConfirmationDialog(activity as MainActivity,R.string.replace_existTitle_orNot.toString(),negative = R.string.no){
                        val id=events1[0].id
                        val title=events1[0].title
                        val iDeleted=activity!!.dbHelper.deleteEvents(arrayOf(id.toString()),false)
                        Log.d(APP_TAG,"customized event deleted both id=$id and those whose parentId is this $id,total count=$iDeleted")
                        addCustomizeEvent(title){
                            activity!!.toast(R.string.customized_event_added)
                            refreshItems()
                        }
                    }
                }
                else
                    addCustomizeEvent(title){
                        activity!!.toast(R.string.customized_event_added)
                        refreshItems()
                    }
            }

        }
        //no else since txt-when click was processed in setupCustomizeEvents

    }

    private fun refreshItems() {
        setupCustomizeEventList()
    }


    private fun setupReminderSound(isEnabled: Boolean = true) {
        tv_settings_reminder_sound.isEnabled = isEnabled

        val noRingtone = res.getString(R.string.no_ringtone_selected)
        if (activity!!.config.reminderSound.isEmpty()) {
            tv_settings_reminder_sound.text = noRingtone
        } else {
            tv_settings_reminder_sound.text = RingtoneManager.getRingtone(activity as MainActivity, Uri.parse(activity!!.config.reminderSound))?.getTitle(activity as MainActivity) ?: noRingtone
        }
        tv_settings_reminder_sound.setOnClickListener {
            Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, res.getString(R.string.reminder_sound_label))
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(activity!!.config.reminderSound))

                if (resolveActivity(activity!!.packageManager) != null)
                    startActivityForResult(this, GET_RINGTONE_URI)
                else {
                    activity!!.toast(R.string.no_ringtone_picker)
                }
            }
        }
    }

    private fun setupVibrate(isEnabled: Boolean = true) {
        sc_settings_reminder_vibrate.isEnabled = isEnabled
        rl_settings_reminder_vibrate_holder.isEnabled = isEnabled
        if (isEnabled) {
            rl_settings_reminder_vibrate_holder.setOnClickListener {
                sc_settings_reminder_vibrate.toggle()
                activity!!.config.vibrateOnReminder = sc_settings_reminder_vibrate.isChecked
            }
            sc_settings_reminder_vibrate.isChecked = activity!!.config.vibrateOnReminder
        } else
            sc_settings_reminder_vibrate.isChecked = false
        activity!!.config.vibrateOnReminder = sc_settings_reminder_vibrate.isChecked
    }

    private fun setupReminderTime(isEnabled: Boolean = true) {
        acs_reminderTs.isEnabled = isEnabled
        val reminderTs=activity!!.config.reminderTs
        if (isEnabled){
            val reminderTs_array = resources.getStringArray(R.array.reminderTs)
            val reminderTs_adapter = ArrayAdapter<CharSequence>(activity as MainActivity, android.R.layout.simple_spinner_item, reminderTs_array)
            reminderTs_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            acs_reminderTs.adapter = reminderTs_adapter
            when (reminderTs){
                REMINDER_INITIAL_TS -> acs_reminderTs.setSelection(0)
                REMINDER_INITIAL_TS_PLUS_30MIN -> acs_reminderTs.setSelection(1)
                REMINDER_INITIAL_TS_PLUS_60MIN -> acs_reminderTs.setSelection(2)
                REMINDER_INITIAL_TS_PLUS_90MIN -> acs_reminderTs.setSelection(3)
                REMINDER_INITIAL_TS_PLUS_120MIN -> acs_reminderTs.setSelection(4)
                REMINDER_INITIAL_TS_PLUS_150MIN -> acs_reminderTs.setSelection(5)
                REMINDER_INITIAL_TS_PLUS_180MIN -> acs_reminderTs.setSelection(6)
                REMINDER_INITIAL_TS_PLUS_210MIN -> acs_reminderTs.setSelection(7)
            }
        }
        acs_reminderTs.onItemSelectedListener = this

    }

    private fun setupReminderSwitch(isChecked: Boolean = true) {
        sc_settings_reminder_switch.isChecked = isChecked //no trigger clicklistener when enter here FIRST
//        settings_reminder_switch.textSize=activity!!.config.fontSize.toFloat()
        setupReminderTime(isChecked)
        setupVibrate(isChecked)
        setupReminderSound(isChecked)

        rl_settings_reminder_switch_holder.setOnClickListener {
            sc_settings_reminder_switch.toggle()
            val reminderOnOff = sc_settings_reminder_switch.isChecked
            activity!!.config.reminderSwitch = reminderOnOff
            setupReminderTime(reminderOnOff)
            setupVibrate(reminderOnOff)
            setupReminderSound(reminderOnOff)
            if (reminderOnOff)
                Thread {
                    activity!!.processEventRemindersNotification(activity!!.dbHelper.getEventsToExport(false))
                }.start()
            else
                Thread {
                    activity!!.cancelAllNotification()
                }.start()
        }
    }

    private fun setupReminerGeneral() {
        val reminderOnOff = activity!!.config.reminderSwitch
        setupReminderSwitch(reminderOnOff)
    }

    private fun setupCustomizeEvent() {
        val whomfor_array = resources.getStringArray(R.array.whomfor)
        val whomfor_adapter = ArrayAdapter<CharSequence>(activity as MainActivity, android.R.layout.simple_spinner_item, whomfor_array)
        whomfor_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        acs_customize_event_whomfor.adapter = whomfor_adapter
        acs_customize_event_whomfor.onItemSelectedListener = this

        val whatfor_array = resources.getStringArray(R.array.whatfor)
        val whatfor_adapter = ArrayAdapter<CharSequence>(activity as MainActivity, android.R.layout.simple_spinner_item, whatfor_array)
        whatfor_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        acs_customize_event_whatfor.adapter = whatfor_adapter
        acs_customize_event_whatfor.onItemSelectedListener = this

        tv_settings_customize_event_when.setOnClickListener {
            CustomizeLunarDialog(activity as MainActivity, lunarDate=tv_settings_customize_event_when.value) { lunarDate, gregorianDate ->
                tv_settings_customize_event_when.text = lunarDate
                tv_settings_customize_event_when_gregorian.text = gregorianDate
            }
        }

        setupCustomizeEventList()

        btn_customize_event_add.setOnClickListener(this)
    }

    private fun setupCustomizeEventList() {
        val le=activity!!.dbHelper.getCustomizedEvents().sortedByDescending { event ->event.id  }

        val ceAdapter = CustomizeEventsAdapter(activity as MainActivity, ArrayList<Event>(le) , rv_customize_event,
                {any -> itemModifyClick(any as Event)},{ any -> itemRemoveClick(any as Event)})

        (activity as MainActivity).runOnUiThread {
            tv_settings_placeholder.beVisibleIf(le.isEmpty())
            rv_customize_event.beGoneIf(le.isEmpty())
            ceAdapter.addVerticalDividers(true);
            rv_customize_event.adapter=ceAdapter }


    }

//    private fun showReminderDialog() {
//        activity!!.showEventReminderDialog(mReminderMinutes) {
//            mReminderMinutes = it
//            tv_settings_builtin_events_reminder_time.text = activity!!.getFormattedMinutes(mReminderMinutes)
//            activity!!.config.currentReminderMinutes = mReminderMinutes
//            Thread {
//                activity!!.dbHelper.updateEventStartTS(mReminderMinutes)
//                activity!!.processEventRemindersNotification(activity!!.dbHelper.getEventsToExport(false))
//            }.start()
//        }
//    }


    private fun itemModifyClick(event: Event) {
        val title=event.title.split(" ")
        val whomfor = title[0]
        val whatfor = title[1]
        val lunarDate = event.lunar
        val id=event.id.toString()

        CustomizeEventDialog(activity as MainActivity, whomfor, whatfor, lunarDate) { cb_whomfor, cb_whatfor, cb_lunarDate, cb_gregorianDate ->
            val title=cb_whomfor+" "+cb_whatfor
            val cb_startTs=Formatter.getDayStartTS(cb_gregorianDate)
            if (title == whomfor+" "+whatfor && lunarDate == cb_lunarDate) return@CustomizeEventDialog
            val iDeleted=activity!!.dbHelper.deleteEvents(arrayOf(id),false)
            Log.d(APP_TAG,"customized event deleted both id=$id and those whose parentId is this $id,total count=$iDeleted")
            addCustomizeEvent(title,cb_startTs,cb_lunarDate){
                activity!!.toast(R.string.customized_event_updated)
                refreshItems()
            }
        }
    }

    private fun itemRemoveClick(event: Event) {
        val id=event.id.toString()
        val iDeleted=activity!!.dbHelper.deleteEvents(arrayOf(id),false)
        activity!!.toast(R.string.customized_event_deleted)
        Log.d(APP_TAG,"customized event deleted both id=$id and those whose parentId is this $id,total count=$iDeleted")
        refreshItems()
    }

    private fun addCustomizeEvent(title:String,inStartTs:Int=0,inLunarDate:String="",callback:()->Unit) {
        var startTs=0;var endTs= 0; var parentId=0
        var iGregDayofMonth=0; var iGregMonth=0;var iGregYear=0
        var ggMonth="";var ggDayofMonth=""
        val lundarDate: String
        var idsToProcessNotification = ArrayList<String>()

        if (inStartTs == 0) {
            val gregorian = tv_settings_customize_event_when_gregorian.value

            startTs = Formatter.getDayStartTS(gregorian)
        } else {
            startTs = inStartTs
        }

        if (inLunarDate.isEmpty()) {
            lundarDate = tv_settings_customize_event_when.value
        } else {
            lundarDate = inLunarDate
        }

        var yearsToAdd = (activity!!.getNowSeconds() - startTs) / YEAR

        if (yearsToAdd == 0) yearsToAdd++
        else if ((DateTime().dayOfYear) < (DateTime(startTs.toLong()).dayOfYear)) yearsToAdd++

        var lunarYear = lundarDate.substring(0, 4).toInt()
        val lunarMonth = lundarDate.substring(4, 6).toInt()
        val lunarDay = lundarDate.substring(6, 8).toInt()

        for (i in 0..YEARS_LIMIT_CUSTOMIZE_EVENT) {
            lunarYear += yearsToAdd
            val myCal = ChineseCalendar(true, lunarYear, lunarMonth, lunarDay)
            iGregYear = myCal.get(Calendar.YEAR)
            iGregMonth = myCal.get(Calendar.MONTH)+1
            iGregDayofMonth = myCal.get(Calendar.DAY_OF_MONTH)
            if (iGregMonth<10) ggMonth="0$iGregMonth" else ggMonth="$iGregMonth"
            if (iGregDayofMonth<10) ggDayofMonth="0$iGregDayofMonth" else ggDayofMonth="$iGregDayofMonth"

            startTs = Formatter.getDayStartTS("$iGregYear$ggMonth$ggDayofMonth")
            endTs = startTs + 1
            if (i==0) parentId=0
            val event = Event(0, startTs, endTs, title = title,  source = SOURCE_CUSTOMIZE_ANNIVERSARY,
                    color = Color.BLUE, lunar = lundarDate,parentId =parentId )
            activity!!.dbHelper.insert(event, false) {
                Log.d(APP_TAG, "customized event inserted with id=$it,title=$title,startTs=$startTs")
                idsToProcessNotification.add(it.toString())
                if (i==0) parentId=it
            }
        }

        if (context!!.config.reminderSwitch)
            activity!!.processEventRemindersNotification(idsToProcessNotification)
        callback()
    }

    private fun updateReminerTs(selectedItemPosition:Int){
        var reminderTs=0
        when (selectedItemPosition){
            0 -> reminderTs= REMINDER_INITIAL_TS
            1 -> reminderTs= REMINDER_INITIAL_TS_PLUS_30MIN
            2 -> reminderTs= REMINDER_INITIAL_TS_PLUS_60MIN
            3 -> reminderTs= REMINDER_INITIAL_TS_PLUS_90MIN
            4 -> reminderTs= REMINDER_INITIAL_TS_PLUS_120MIN
            5 -> reminderTs= REMINDER_INITIAL_TS_PLUS_150MIN
            6 -> reminderTs= REMINDER_INITIAL_TS_PLUS_180MIN
            7 -> reminderTs= REMINDER_INITIAL_TS_PLUS_210MIN
        }


        if (reminderTs != activity!!.config.reminderTs){
            activity!!.config.reminderTs = reminderTs
            // no need check config.reminderSwitch=true since program cannot come here if the switch is off
            Thread {
                activity!!.processEventRemindersNotification(activity!!.dbHelper.getEventsToExport(false))
            }.start()

            context!!.toast(R.string.reminder_time_udpated)
        }
    }
    //all below are for placeholder purpose
    override fun goToToday() {
    }

    override fun refreshEvents() {
    }

    override fun shouldGoToTodayBeVisible(): Boolean {
        return true
    }

    override fun updateActionBarTitle() {
    }


    override fun getNewEventDayCode(): String {
        return ""
    }
}
