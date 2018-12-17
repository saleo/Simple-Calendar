package com.simplemobiletools.calendar.activities

import android.app.SearchManager
import android.content.*
import android.content.pm.ActivityInfo
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import at.bitfire.ical4android.AndroidCalendar
import at.bitfire.ical4android.CalendarStorageException
import at.bitfire.icsdroid.AppAccount
import at.bitfire.icsdroid.AppAccount.account
import at.bitfire.icsdroid.Constants
import at.bitfire.icsdroid.db.LocalCalendar
import com.simplemobiletools.calendar.BuildConfig
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.R.string.status_day
import com.simplemobiletools.calendar.R.string.status_month
import com.simplemobiletools.calendar.adapters.EventListAdapter
import com.simplemobiletools.calendar.dialogs.ExportEventsDialog
import com.simplemobiletools.calendar.dialogs.FilterEventTypesDialog
import com.simplemobiletools.calendar.dialogs.ImportEventsDialog
import com.simplemobiletools.calendar.extensions.*
import com.simplemobiletools.calendar.fragments.*
import com.simplemobiletools.calendar.helpers.*
import com.simplemobiletools.calendar.helpers.Formatter
import com.simplemobiletools.calendar.models.Event
import com.simplemobiletools.calendar.models.EventType
import com.simplemobiletools.calendar.models.ListEvent
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.commons.models.Release
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.top_navigation.*
import org.joda.time.DateTime
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

const val MY_PERMISSIONS_REQUEST_READ_CALENDAR=1

class MainActivity : SimpleActivity(), RefreshRecyclerViewListener {
//    var mCurrentShownMonth="";var mCurrentShownDay=""
    private val CALDAV_SYNC_DELAY = 1000L
    private val SKCAL_NON_EXIST=1
    private val SKCAL_CHECK_ERROR=0
    private val SK_CREATE_FAILED=0
    private val SKCAL_URL="http://tp.euse.cn/1vevent.ics"

    private lateinit var layout: View

    private var showRefreshToastOnCalDataChange = true
    private var showRefreshToastOnActivityResume = false
    private var mShouldFilterBeVisible = false
    private var mIsSearchOpen = false
    private var mLatestSearchQuery = ""
    private var mCalDAVSyncHandler = Handler()
    private var mSearchMenuItem: MenuItem? = null
    private var shouldGoToTodayBeVisible = false
    private var goToTodayButton: MenuItem? = null
    private var currentFragments = ArrayList<Fragment>()

    private var mStoredTextColor = 0
    private var mStoredBackgroundColor = 0
    private var mStoredPrimaryColor = 0
    private var mStoredDayCode = ""
    private var mStoredIsSundayFirst = false
    private var mStoredUse24HourFormat = false
    private var mStoredUseEnglish = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched()
        if (baseConfig.appRunCount==1)
            config.reminderTs= REMINDER_INITIAL_TS

        checkWhatsNewDialog()
        //calendar_fab.beVisibleIf(config.storedView != YEARLY_VIEW)

        getStoredStateVariables()
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {

            val uri = intent.data
            if (uri.authority == "com.android.calendar") {
                // clicking date on a third party widget: content://com.android.calendar/time/1507309245683
                if (intent?.extras?.getBoolean("DETAIL_VIEW", false) == true) {
                    val timestamp = uri.pathSegments.last()
                    if (timestamp.areDigitsOnly()) {
                        openDayAt(timestamp.toLong())
                        return
                    }
                }
            } else {
                tryImportEventsFromFile(uri)
            }
        }

        if (!checkOpenIntents()) {
            updateView(config.storedView)
        }

        handlePermission(PERMISSION_WRITE_CALENDAR) {
            if (it) {
                handlePermission(PERMISSION_READ_CALENDAR) {
                    if (it) {
                        var res=checkSkCalExist()
                        if (res!=SKCAL_NON_EXIST && res!=SKCAL_CHECK_ERROR){

                            val extras = Bundle(2)
                            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                            extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
                            ContentResolver.requestSync(AppAccount.account, CalendarContract.AUTHORITY, extras)
                        } else if (res==SKCAL_NON_EXIST){
                            res=createSkCalendar()
                            if (res!=SK_CREATE_FAILED)
                                Toast.makeText(this, getString(R.string.add_calendar_created), Toast.LENGTH_LONG).show()
                            else
                                Toast.makeText(this, getString(R.string.add_calendar_failed), Toast.LENGTH_LONG).show()
                        } else{
                            Toast.makeText(this, getString(R.string.check_calendar_failed), Toast.LENGTH_LONG).show()
                        }
                        this.config.caldavSync=true
                        this.config.lastUsedCaldavCalendar=res
                        this.config.caldavSyncedCalendarIDs=res.toString()
                    }
                }
            }
            else{
                finish()
            }
        }

        if (!hasPermission(PERMISSION_WRITE_CALENDAR) || !hasPermission(PERMISSION_READ_CALENDAR)) {
            config.caldavSync = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (mStoredUseEnglish != config.useEnglish) {
            restartActivity()
            return
        }

        dbHelper.getEventTypes {
            mShouldFilterBeVisible = it.size > 1 || config.displayEventTypes.isEmpty()
        }

        if (config.storedView != EVENTS_LIST_VIEW) {
            updateTextColors(calendar_coordinator)
            if (config.storedView == WEEKLY_VIEW) {
                if (mStoredIsSundayFirst != config.isSundayFirst || mStoredUse24HourFormat != config.use24hourFormat) {
                    updateView(WEEKLY_VIEW)
                }
            }
        }

        updateWidgets()

        calendar_fab.setColors(config.textColor, getAdjustedPrimaryColor(), config.backgroundColor)
        search_holder.background = ColorDrawable(config.backgroundColor)

        refreshCalDAVCalendars(showRefreshToastOnActivityResume)
        getStoredStateVariables()

    }

    override fun onPause() {
        super.onPause()
        getStoredStateVariables()
    }

    override fun onStop() {
        super.onStop()
        mCalDAVSyncHandler.removeCallbacksAndMessages(null)
        contentResolver.unregisterContentObserver(calDAVSyncObserver)
        closeSearch()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.apply {
            goToTodayButton = findItem(R.id.go_to_today)
            findItem(R.id.filter).isVisible = mShouldFilterBeVisible
            findItem(R.id.go_to_today).isVisible = shouldGoToTodayBeVisible && config.storedView != EVENTS_LIST_VIEW
        }

        setupSearch(menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu!!.apply {
            findItem(R.id.refresh_caldav_calendars).isVisible = config.caldavSync
        }

        return true
    }

    override fun onBackPressed() {
        val size=currentFragments.size
        if (size > 1) {
            currentFragments.removeAt(size-1)
            val f=currentFragments.last()
            supportFragmentManager.beginTransaction().replace(R.id.fragments_holder, f).commitNow()

        } else {
            val fragment=currentFragments.last()
            currentFragments.clear()
            supportFragmentManager.beginTransaction().remove(fragment).commitNow()
            exitProcess(0)
        }
    }

    private fun getStoredStateVariables() {
        config.apply {
            mStoredUseEnglish = useEnglish
            mStoredIsSundayFirst = isSundayFirst
            mStoredTextColor = textColor
            mStoredPrimaryColor = primaryColor
            mStoredBackgroundColor = backgroundColor
            mStoredUse24HourFormat = use24hourFormat
        }
        mStoredDayCode = Formatter.getTodayCode(applicationContext)
    }

    private fun setupSearch(menu: Menu) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchMenuItem = menu.findItem(R.id.search)
        (mSearchMenuItem!!.actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isSubmitButtonEnabled = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    if (mIsSearchOpen) {
                        searchQueryChanged(newText)
                    }
                    return true
                }
            })
        }

        MenuItemCompat.setOnActionExpandListener(mSearchMenuItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                mIsSearchOpen = true
                search_holder.beVisible()
                //calendar_fab.beGone()
                searchQueryChanged("")
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                mIsSearchOpen = false
                search_holder.beGone()
                //calendar_fab.beVisible()
                return true
            }
        })
    }

    private fun closeSearch() {
        mSearchMenuItem?.collapseActionView()
    }

    private fun checkOpenIntents(): Boolean {
        val dayCodeToOpen = intent.getStringExtra(DAY_CODE) ?: ""
        val openMonth = intent.getBooleanExtra(OPEN_MONTH, false)
        intent.removeExtra(OPEN_MONTH)
        intent.removeExtra(DAY_CODE)
        if (dayCodeToOpen.isNotEmpty()) {
            //calendar_fab.beVisible()
            config.storedView = if (openMonth) MONTHLY_VIEW else DAILY_VIEW
            updateView(config.storedView,dayCodeToOpen)
            return true
        }

        val eventIdToOpen = intent.getIntExtra(EVENT_ID, 0)
        val eventOccurrenceToOpen = intent.getIntExtra(EVENT_OCCURRENCE_TS, 0)
        intent.removeExtra(EVENT_ID)
        intent.removeExtra(EVENT_OCCURRENCE_TS)
        if (eventIdToOpen != 0 && eventOccurrenceToOpen != 0) {
            Intent(this, EventActivity::class.java).apply {
                putExtra(EVENT_ID, eventIdToOpen)
                putExtra(EVENT_OCCURRENCE_TS, eventOccurrenceToOpen)
                startActivity(this)
            }
        }

        return false
    }

    private fun showViewDialog() {
        val items = arrayListOf(
                RadioItem(DAILY_VIEW, getString(R.string.daily_view)),
                RadioItem(WEEKLY_VIEW, getString(R.string.weekly_view)),
                RadioItem(MONTHLY_VIEW, getString(R.string.monthly_view)),
                RadioItem(YEARLY_VIEW, getString(R.string.yearly_view)),
                RadioItem(EVENTS_LIST_VIEW, getString(R.string.simple_event_list)))

        RadioGroupDialog(this, items, config.storedView) {
            //calendar_fab.beVisibleIf(it as Int != YEARLY_VIEW)
            resetActionBarTitle()
            closeSearch()
            updateView(it as Int)
            shouldGoToTodayBeVisible = false
            invalidateOptionsMenu()
        }
    }

    fun goToToday() {
        val f=currentFragments.last()
        if (f is MyFragmentHolder)
            f.goToToday()
    }

    private fun resetActionBarTitle() {
        supportActionBar?.title = getString(R.string.app_launcher_name)
        supportActionBar?.subtitle = ""
    }

    private fun showFilterDialog() {
        FilterEventTypesDialog(this) {
            refreshViewPager()
        }
    }

    fun toggleGoToTodayVisibility(beVisible: Boolean) {
        shouldGoToTodayBeVisible = beVisible
        if (goToTodayButton?.isVisible != beVisible) {
            invalidateOptionsMenu()
        }
    }

    private fun refreshCalDAVCalendars(showRefreshToast: Boolean) {
        if (showRefreshToast) {
            toast(R.string.refreshing)
        }

        syncCalDAVCalendars(this, calDAVSyncObserver)
        scheduleCalDAVSync(true)
    }

    private val calDAVSyncObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            Log.d(Constants.TAG,"calDavSync onChange() observed")
            if (!selfChange) {
                mCalDAVSyncHandler.removeCallbacksAndMessages(null)
                mCalDAVSyncHandler.postDelayed({
                    recheckCalDAVCalendars {
                        refreshViewPager()
                        if (showRefreshToastOnCalDataChange) {
                            toast(R.string.refreshing_complete)
                        }
                    }
                }, CALDAV_SYNC_DELAY)
            }
        }
    }

    private fun handleParseResult(result: IcsImporter.ImportResult) {
        toast(when (result) {
            IcsImporter.ImportResult.IMPORT_OK -> R.string.holidays_imported_successfully
            IcsImporter.ImportResult.IMPORT_PARTIAL -> R.string.importing_some_holidays_failed
            else -> R.string.importing_holidays_failed
        }, Toast.LENGTH_LONG)
    }

    private fun addContactEvents(birthdays: Boolean, callback: (Int) -> Unit) {
        var eventsAdded = 0
        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Event.CONTACT_ID,
                ContactsContract.CommonDataKinds.Event.CONTACT_LAST_UPDATED_TIMESTAMP,
                ContactsContract.CommonDataKinds.Event.START_DATE)

        val selection = "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ?"
        val type = if (birthdays) ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY else ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY
        val selectionArgs = arrayOf(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, type.toString())
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor?.moveToFirst() == true) {
                val dateFormats = getDateFormats()
                val existingEvents = if (birthdays) dbHelper.getBirthdays() else dbHelper.getAnniversaries()
                val importIDs = existingEvents.map { it.importId }
                val eventTypeId = if (birthdays) getBirthdaysEventTypeId() else getAnniversariesEventTypeId()

                do {
                    val contactId = cursor.getIntValue(ContactsContract.CommonDataKinds.Event.CONTACT_ID).toString()
                    val name = cursor.getStringValue(ContactsContract.Contacts.DISPLAY_NAME)
                    val startDate = cursor.getStringValue(ContactsContract.CommonDataKinds.Event.START_DATE)

                    for (format in dateFormats) {
                        try {
                            val formatter = SimpleDateFormat(format, Locale.getDefault())
                            val date = formatter.parse(startDate)
                            if (date.year < 70)
                                date.year = 70

                            val timestamp = (date.time / 1000).toInt()
                            val source = if (birthdays) SOURCE_CONTACT_BIRTHDAY else SOURCE_CONTACT_ANNIVERSARY
                            val lastUpdated = cursor.getLongValue(ContactsContract.CommonDataKinds.Event.CONTACT_LAST_UPDATED_TIMESTAMP)
                            val event = Event(0, timestamp, timestamp, name, repeatInterval = YEAR, importId = contactId, flags = FLAG_ALL_DAY,
                                    eventType = eventTypeId, lastUpdated = lastUpdated, source = source, lunar = "")

                            if (!importIDs.contains(contactId)) {
                                dbHelper.insert(event, false) {
                                    eventsAdded++
                                }
                            }
                            break
                        } catch (e: Exception) {
                        }
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            showErrorToast(e)
        } finally {
            cursor?.close()
        }

        runOnUiThread {
            callback(eventsAdded)
        }
    }

    private fun getBirthdaysEventTypeId(): Int {
        val birthdays = getString(R.string.birthdays)
        var eventTypeId = dbHelper.getEventTypeIdWithTitle(birthdays)
        if (eventTypeId == -1) {
            val eventType = EventType(0, birthdays, resources.getColor(R.color.default_birthdays_color))
            eventTypeId = dbHelper.insertEventType(eventType)
        }
        return eventTypeId
    }

    private fun getAnniversariesEventTypeId(): Int {
        val anniversaries = getString(R.string.anniversaries)
        var eventTypeId = dbHelper.getEventTypeIdWithTitle(anniversaries)
        if (eventTypeId == -1) {
            val eventType = EventType(0, anniversaries, resources.getColor(R.color.default_anniversaries_color))
            eventTypeId = dbHelper.insertEventType(eventType)
        }
        return eventTypeId
    }

    fun updateView(view:Int,dayCode: String? = Formatter.getTodayCode(applicationContext)) {
        config.storedView = view

        val fragment = getFragmentsHolder()

        currentFragments.forEach {
            supportFragmentManager.beginTransaction().remove(it).commitNow()
        }

        currentFragments.clear()
        currentFragments.add(fragment as Fragment)

        //since only Monthly_view and event_list_view can come here
        val bundle=Bundle()
        bundle.putString(DAY_CODE, dayCode)

        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().add(R.id.fragments_holder, fragment).commitNow()
    }

    fun openMonthFromYearly(dateTime: DateTime) {
    }

    fun openFragment(dateTime: DateTime=DateTime(),view: Int= ABOUT_HEALTH_VIEW){
        if ((config.storedView == ABOUT_HEALTH_VIEW) && (view == ABOUT_HEALTH_VIEW)) return
        config.storedView=view
        val fragment=HealthFragment()
        currentFragments.add(fragment)
        val bundle = Bundle()
        bundle.putString(DAY_CODE, Formatter.getDayCodeFromDateTime(dateTime))
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.fragments_holder, fragment).commitNow()
    }

    fun openFragmentHolder(dateTime: DateTime=DateTime(), view: Int= MONTHLY_VIEW) {
        var fragment:MyFragmentHolder
        if ((config.storedView == DAILY_VIEW) && (view == DAILY_VIEW))
        else if ((config.storedView == MONTHLY_VIEW) && (view == MONTHLY_VIEW))
        else if ((config.storedView == EVENTS_LIST_VIEW) && (view == EVENTS_LIST_VIEW))
        else if ((config.storedView == QINGXIN_VIEW) && (view == QINGXIN_VIEW))
        else if ((config.storedView == ABOUT_VIEW) && (view == ABOUT_VIEW))
        else if ((config.storedView == ABOUT_INTRO_VIEW) && (view == ABOUT_INTRO_VIEW))
        else if ((config.storedView == ABOUT_CREDIT_VIEW) && (view == ABOUT_CREDIT_VIEW))
        else if ((config.storedView == ABOUT_LICENSE_VIEW) && (view == ABOUT_LICENSE_VIEW))
        else if ((config.storedView == SETTINGS_VIEW) && (view == SETTINGS_VIEW))
        {
            return
        }

        config.storedView= view
        when (view){
            DAILY_VIEW -> fragment = DayFragmentsHolder()
            MONTHLY_VIEW -> fragment = MonthFragmentsHolder()
            EVENTS_LIST_VIEW -> fragment = EventListFragmentsHolder()
            QINGXIN_VIEW -> fragment = QingxinFragment()
            ABOUT_VIEW -> fragment=AboutFragment()
            ABOUT_INTRO_VIEW -> fragment=IntroFragment()
            ABOUT_CREDIT_VIEW -> fragment=CreditFragment()
            ABOUT_LICENSE_VIEW -> fragment=LicenseFragment()
            SETTINGS_VIEW -> fragment=SettingsFragment()
            else -> fragment=MonthFragmentsHolder()
        }

        currentFragments.add(fragment as Fragment)
        val bundle = Bundle()
        bundle.putString(DAY_CODE, Formatter.getDayCodeFromDateTime(dateTime))
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.fragments_holder, fragment).commitNow()
    }

    private fun getThisWeekDateTime(): String {
        var thisweek = DateTime().withDayOfWeek(1).withTimeAtStartOfDay().minusDays(if (config.isSundayFirst) 1 else 0)
        if (DateTime().minusDays(7).seconds() > thisweek.seconds()) {
            thisweek = thisweek.plusDays(7)
        }
        return thisweek.toString()
    }

    private fun getFragmentsHolder() = when (config.storedView) {
        DAILY_VIEW -> DayFragmentsHolder()
        MONTHLY_VIEW -> MonthFragmentsHolder()
        YEARLY_VIEW -> YearFragmentsHolder()
        EVENTS_LIST_VIEW -> EventListFragmentsHolder()
        QINGXIN_VIEW -> QingxinFragment()
        ABOUT_VIEW -> AboutFragment()
        ABOUT_INTRO_VIEW -> IntroFragment()
        ABOUT_CREDIT_VIEW -> CreditFragment()
        ABOUT_HEALTH_VIEW -> HealthFragment()
        ABOUT_LICENSE_VIEW -> LicenseFragment()
        SETTINGS_VIEW -> SettingsFragment()
        else -> MonthFragmentsHolder()
    }

    private fun refreshViewPager() {
        runOnUiThread {
            if (!isActivityDestroyed()) {
                val f=currentFragments.last()
                if (f is MyFragmentHolder) f.refreshEvents()
            }
        }
    }

    private fun tryImportEvents() {
        handlePermission(PERMISSION_READ_STORAGE) {
            if (it) {
                importEvents()
            }
        }
    }

    private fun importEvents() {
        FilePickerDialog(this) {
            showImportEventsDialog(it)
        }
    }

    private fun tryImportEventsFromFile(uri: Uri) {
        when {
            uri.scheme == "file" -> showImportEventsDialog(uri.path)
            uri.scheme == "content" -> {
                val tempFile = getTempFile()
                if (tempFile == null) {
                    toast(R.string.unknown_error_occurred)
                    return
                }

                val inputStream = contentResolver.openInputStream(uri)
                val out = FileOutputStream(tempFile)
                inputStream.copyTo(out)
                showImportEventsDialog(tempFile.absolutePath)
            }
            else -> toast(R.string.invalid_file_format)
        }
    }

    private fun showImportEventsDialog(path: String) {
        ImportEventsDialog(this, path) {
            if (it) {
                runOnUiThread {
                    updateView(config.storedView)
                }
            }
        }
    }

    private fun tryExportEvents() {
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                exportEvents()
            }
        }
    }

    private fun exportEvents() {
        FilePickerDialog(this, pickFile = false, showFAB = true) {
            ExportEventsDialog(this, it) { exportPastEvents, file, eventTypes ->
                Thread {
                    val events = dbHelper.getEventsToExport(exportPastEvents).filter { eventTypes.contains(it.eventType.toString()) }
                    if (events.isEmpty()) {
                        toast(R.string.no_entries_for_exporting)
                    } else {
                        toast(R.string.exporting)
                        IcsExporter().exportEvents(this, file, events as ArrayList<Event>) {
                            toast(when (it) {
                                IcsExporter.ExportResult.EXPORT_OK -> R.string.exporting_successful
                                IcsExporter.ExportResult.EXPORT_PARTIAL -> R.string.exporting_some_entries_failed
                                else -> R.string.exporting_failed
                            })
                        }
                    }
                }.start()
            }
        }
    }

    private fun searchQueryChanged(text: String) {
        mLatestSearchQuery = text
        search_placeholder_2.beGoneIf(text.length >= 2)
        if (text.length >= 2) {
            dbHelper.getEventsWithSearchQuery(text) { searchedText, events ->
                if (searchedText == mLatestSearchQuery) {
                    runOnUiThread {
                        search_results_list.beVisibleIf(events.isNotEmpty())
                        search_placeholder.beVisibleIf(events.isEmpty())
                        val listItems = getEventListItems(events)
                        val eventsAdapter = EventListAdapter(this, listItems, true, this, search_results_list) {
                            if (it is ListEvent) {
                                Intent(applicationContext, EventActivity::class.java).apply {
                                    putExtra(EVENT_ID, it.id)
                                    startActivity(this)
                                }
                            }
                        }

                        search_results_list.adapter = eventsAdapter
                    }
                }
            }
        } else {
            search_placeholder.beVisible()
            search_results_list.beGone()
        }
    }

    // only used at active search
    override fun refreshItems() {
        searchQueryChanged(mLatestSearchQuery)
        refreshViewPager()
    }

    private fun openDayAt(timestamp: Long) {
        val dayCode = Formatter.getDayCodeFromTS((timestamp / 1000).toInt())
        //calendar_fab.beVisible()
        config.storedView = DAILY_VIEW
        updateView(config.storedView,dayCode)
    }

    private fun getHolidayRadioItems(): ArrayList<RadioItem> {
        val items = ArrayList<RadioItem>()

        LinkedHashMap<String, String>().apply {
            put("Algeria", "algeria.ics")
            put("Argentina", "argentina.ics")
            put("Australia", "australia.ics")
            put("België", "belgium.ics")
            put("Bolivia", "bolivia.ics")
            put("Brasil", "brazil.ics")
            put("Canada", "canada.ics")
            put("Česká republika", "czech.ics")
            put("Deutschland", "germany.ics")
            put("Eesti", "estonia.ics")
            put("España", "spain.ics")
            put("Éire", "ireland.ics")
            put("France", "france.ics")
            put("Hanguk", "southkorea.ics")
            put("Hellas", "greece.ics")
            put("India", "india.ics")
            put("Ísland", "iceland.ics")
            put("Italia", "italy.ics")
            put("Magyarország", "hungary.ics")
            put("Nederland", "netherlands.ics")
            put("日本", "japan.ics")
            put("Norge", "norway.ics")
            put("Österreich", "austria.ics")
            put("Pākistān", "pakistan.ics")
            put("Polska", "poland.ics")
            put("Portugal", "portugal.ics")
            put("Россия", "russia.ics")
            put("Schweiz", "switzerland.ics")
            put("Slovenija", "slovenia.ics")
            put("Slovensko", "slovakia.ics")
            put("Suomi", "finland.ics")
            put("Sverige", "sweden.ics")
            put("United Kingdom", "unitedkingdom.ics")
            put("United States", "unitedstates.ics")

            var i = 0
            for ((country, file) in this) {
                items.add(RadioItem(i++, country, file))
            }
        }

        return items
    }

    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            add(Release(39, R.string.release_39))
            add(Release(40, R.string.release_40))
            add(Release(42, R.string.release_42))
            add(Release(44, R.string.release_44))
            add(Release(46, R.string.release_46))
            add(Release(48, R.string.release_48))
            add(Release(49, R.string.release_49))
            add(Release(51, R.string.release_51))
            add(Release(52, R.string.release_52))
            add(Release(54, R.string.release_54))
            add(Release(57, R.string.release_57))
            add(Release(59, R.string.release_59))
            add(Release(60, R.string.release_60))
            add(Release(62, R.string.release_62))
            add(Release(67, R.string.release_67))
            add(Release(69, R.string.release_69))
            add(Release(71, R.string.release_71))
            add(Release(73, R.string.release_73))
            add(Release(76, R.string.release_76))
            add(Release(77, R.string.release_77))
            add(Release(80, R.string.release_80))
            add(Release(84, R.string.release_84))
            add(Release(86, R.string.release_86))
            add(Release(88, R.string.release_88))
            add(Release(98, R.string.release_98))
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }

    private fun checkSkCalExist():Int{
        var provider= contentResolver.acquireContentProviderClient(CalendarContract.AUTHORITY)
        var res=SKCAL_CHECK_ERROR
        try {
            // currently only filter by account, since cannot find any other criteria suitable or available
            if (!LocalCalendar.findAll(account, provider).isEmpty())
                res=LocalCalendar.findAll(account, provider)[0].id.toInt()
            else
                res=SKCAL_NON_EXIST
        } catch (e: CalendarStorageException) {
            Log.e(Constants.TAG, "Calendar storage exception", e)
            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
        } catch (e: InterruptedException) {
            Log.e(Constants.TAG, "Thread interrupted", e)
            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
        }
        return res
    }

    private fun createSkCalendar(): Int {
        AppAccount.makeAvailable(this)

        val calInfo = ContentValues(9)
        calInfo.put(CalendarContract.Calendars.ACCOUNT_NAME, AppAccount.account.name)
        calInfo.put(CalendarContract.Calendars.ACCOUNT_TYPE, AppAccount.account.type)
        calInfo.put(CalendarContract.Calendars.NAME, SKCAL_URL)
        calInfo.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,getString(R.string.skcal_title) )
        calInfo.put(CalendarContract.Calendars.CALENDAR_COLOR,R.color.lightblue)
        calInfo.put(CalendarContract.Calendars.OWNER_ACCOUNT, AppAccount.account.name)
        calInfo.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        calInfo.put(CalendarContract.Calendars.VISIBLE, 1)
        calInfo.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_READ)

        var res=SK_CREATE_FAILED
        val client: ContentProviderClient? = contentResolver.acquireContentProviderClient(CalendarContract.AUTHORITY)
        return try {
            client?.let {
                val uri = AndroidCalendar.create(AppAccount.account, it, calInfo)
                res=ContentUris.parseId(uri).toInt()
            }
            res
        } catch(e: Exception) {
            Log.e(Constants.TAG, "Couldn't create calendar", e)
//            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
            res
        } finally {
            client?.release()
            res
        }
    }

    fun updateTopBottom(time: DateTime=DateTime.now(), view: Int)
    {
        updateTop(time,view)
        updateBottom(time,view)
    }

    private fun updateTop(time:DateTime, view:Int){
        if (view != MONTHLY_VIEW && view != DAILY_VIEW && view != EVENTS_LIST_VIEW && view != QINGXIN_VIEW) {
            img_top.setImageResource(R.drawable.sk_banner)
            tv_month_number.visibility=View.GONE
            day_monthly_number.visibility=View.GONE
            return
        }

        tv_month_number.visibility=View.VISIBLE

        val intYear = time.year
        val iMonth = time.monthOfYear

        when (intYear){
            2018 -> img_top.setImageResource(R.drawable.sk2018)
            2019 -> img_top.setImageResource(R.drawable.sk2019)
            else -> img_top.setImageResource(R.drawable.sk_banner)
        }
        
        tv_month_number.text=iMonth.toString()+resources.getString(status_month)

        if (view == DAILY_VIEW) {
            day_monthly_number.visibility = View.VISIBLE
            day_monthly_number.text=time.dayOfMonth.toString()+resources.getString(status_day)
            day_monthly_number.textSize=config.getFontSize()*1.07f
        }else
            day_monthly_number.visibility=View.GONE

    }

    private fun updateBottom(time:DateTime, view:Int){
        setupBottomButtonBar(time)

        if (view != MONTHLY_VIEW && view != DAILY_VIEW && view != EVENTS_LIST_VIEW){
            ll_bottom_sentense_holder.visibility=View.GONE
            rl_bottom_copyright_holder.visibility=View.VISIBLE
            val year = Calendar.getInstance().get(Calendar.YEAR)
            about_copyright.text = String.format(getString(R.string.copyright), year)
            return
        }

        val intYear = time.year
        val iMonth = time.monthOfYear
        var mBottomSentences: Array<String>
        val res = resources

        ll_bottom_sentense_holder.visibility=View.VISIBLE
        rl_bottom_copyright_holder.visibility=View.GONE

        mBottomSentences=res.getStringArray(R.array.bottom_sentences_digest)
        bottom_sentense0.textSize = config.getFontSize()*1.01.toFloat()
        bottom_sentense1.textSize = config.getFontSize()*1.01.toFloat()
        bottom_sentense2.textSize = config.getFontSize()*1.01.toFloat()

        bottom_sentense0.setOnClickListener {
            openFragmentHolder(time, QINGXIN_VIEW)
        }
        bottom_sentense1.setOnClickListener {
            openFragmentHolder(time, QINGXIN_VIEW)
        }
        bottom_sentense2.setOnClickListener {
            openFragmentHolder(time, QINGXIN_VIEW)
        }

        if (intYear==2016 || intYear ==2018){
            bottom_sentense0.text=mBottomSentences[3*(iMonth-1)]
            bottom_sentense1.text=mBottomSentences[3*(iMonth-1)+1]
            bottom_sentense2.text=mBottomSentences[3*(iMonth-1)+2]
        }else if (intYear==2017 || intYear==2019){
            bottom_sentense0.text=mBottomSentences[3*(iMonth-1)+36]
            bottom_sentense1.text=mBottomSentences[3*(iMonth-1)+37]
            bottom_sentense2.text=mBottomSentences[3*(iMonth-1)+38]
        }else{
            bottom_sentense0.text=mBottomSentences[3*(iMonth-1)]
            bottom_sentense1.text=mBottomSentences[3*(iMonth-1)+1]
            bottom_sentense2.text=mBottomSentences[3*(iMonth-1)+2]
        }
    }
}
