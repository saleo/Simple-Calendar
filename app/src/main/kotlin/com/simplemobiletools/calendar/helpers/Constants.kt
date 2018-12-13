package com.simplemobiletools.calendar.helpers

import com.simplemobiletools.calendar.R
import com.simplemobiletools.commons.helpers.DAY_SECONDS

const val LOW_ALPHA = .3f
const val MEDIUM_ALPHA = .6f
const val STORED_LOCALLY_ONLY = 0

const val DAY_CODE = "day_code"
const val YEAR_LABEL = "year"
const val EVENT_ID = "event_id"
const val EVENT_OCCURRENCE_TS = "event_occurrence_ts"
const val NEW_EVENT_START_TS = "new_event_start_ts"
const val WEEK_START_TIMESTAMP = "week_start_timestamp"
const val NEW_EVENT_SET_HOUR_DURATION = "new_event_set_hour_duration"
const val WEEK_START_DATE_TIME = "week_start_date_time"
const val CALDAV = "Caldav"
const val OPEN_MONTH = "open_month"

const val MONTHLY_VIEW = 1
const val YEARLY_VIEW = 2
const val EVENTS_LIST_VIEW = 3
const val WEEKLY_VIEW = 4
const val DAILY_VIEW = 5
const val QINGXIN_VIEW=6
const val ABOUT_VIEW=7
const val ABOUT_INTRO_VIEW=8
const val ABOUT_CREDIT_VIEW=9
const val ABOUT_HEALTH_VIEW=10
const val ABOUT_LICENSE_VIEW=11
const val SETTINGS_VIEW=12

const val REMINDER_OFF = -1

const val DAY = 86400
const val WEEK = 604800
const val MONTH = 2592001    // exact value not taken into account, Joda is used for adding months and years
const val YEAR = 31536000

const val DAY_MINUTES = 24 * 60
const val WEEK_SECONDS = 7 * DAY_SECONDS

// Shared Preferences
const val USE_24_HOUR_FORMAT = "use_24_hour_format"
const val SUNDAY_FIRST = "sunday_first"
const val WEEK_NUMBERS = "week_numbers"
const val START_WEEKLY_AT = "start_weekly_at"
const val END_WEEKLY_AT = "end_weekly_at"
const val VIBRATE = "vibrate"
const val REMINDER_SOUND = "reminder_sound"
const val VIEW = "view"
const val REMINDER_MINUTES = "reminder_minutes"
const val REMINDER_MINUTES_2 = "reminder_minutes_2"
const val REMINDER_MINUTES_3 = "reminder_minutes_3"
const val DISPLAY_EVENT_TYPES = "display_event_types"
const val FONT_SIZE = "font_size"
const val CALDAV_SYNC = "caldav_sync"
const val CALDAV_SYNCED_CALENDAR_IDS = "caldav_synced_calendar_ids"
const val LAST_USED_CALDAV_CALENDAR = "last_used_caldav_calendar"
const val SNOOZE_DELAY = "snooze_delay"
const val DISPLAY_PAST_EVENTS = "display_past_events"
const val REPLACE_DESCRIPTION = "replace_description"
const val USE_SAME_SNOOZE = "use_same_snooze"
const val REMINDER_UNIFIED_TIME = "reminder_unified_time"
const val CURRENT_REMINDER_MINUTES ="current_reminder_minutes"
val letterIDs = intArrayOf(R.string.sunday_letter, R.string.monday_letter, R.string.tuesday_letter, R.string.wednesday_letter,
        R.string.thursday_letter, R.string.friday_letter, R.string.saturday_letter)

// repeat_rule for weekly repetition
const val MONDAY = 1
const val TUESDAY = 2
const val WEDNESDAY = 4
const val THURSDAY = 8
const val FRIDAY = 16
const val SATURDAY = 32
const val SUNDAY = 64
const val EVERY_DAY = 127

// repeat_rule for monthly repetition
const val REPEAT_MONTH_SAME_DAY = 1                   // ie 25th every month
const val REPEAT_MONTH_ORDER_WEEKDAY_USE_LAST = 2     // ie every xth sunday. 4th if a month has 4 sundays, 5th if 5
const val REPEAT_MONTH_LAST_DAY = 3                   // ie every last day of the month
const val REPEAT_MONTH_ORDER_WEEKDAY = 4              // ie every 4th sunday, even if a month has 4 sundays only (will stay 4th even at months with 5)

// special event flags
const val FLAG_ALL_DAY = 1

// constants related to ICS file exporting / importing
const val BEGIN_CALENDAR = "BEGIN:VCALENDAR"
const val END_CALENDAR = "END:VCALENDAR"
const val CALENDAR_PRODID = "PRODID:-//Simple Mobile Tools//NONSGML Event Calendar//EN"
const val CALENDAR_VERSION = "VERSION:2.0"
const val BEGIN_EVENT = "BEGIN:VEVENT"
const val END_EVENT = "END:VEVENT"
const val BEGIN_ALARM = "BEGIN:VALARM"
const val END_ALARM = "END:VALARM"
const val DTSTART = "DTSTART"
const val DTEND = "DTEND"
const val LAST_MODIFIED = "LAST-MODIFIED"
const val DURATION = "DURATION:"
const val SUMMARY = "SUMMARY"
const val DESCRIPTION = "DESCRIPTION:"
const val UID = "UID:"
const val ACTION = "ACTION:"
const val TRIGGER = "TRIGGER:"
const val RRULE = "RRULE:"
const val CATEGORIES = "CATEGORIES:"
const val STATUS = "STATUS:"
const val EXDATE = "EXDATE"
const val BYDAY = "BYDAY"
const val BYMONTHDAY = "BYMONTHDAY"
const val LOCATION = "LOCATION:"

// this tag isn't a standard ICS tag, but there's no official way of adding a category color in an ics file
const val CATEGORY_COLOR = "CATEGORY_COLOR:"

const val DISPLAY = "DISPLAY"
const val FREQ = "FREQ"
const val UNTIL = "UNTIL"
const val COUNT = "COUNT"
const val INTERVAL = "INTERVAL"
const val CONFIRMED = "CONFIRMED"
const val VALUE = "VALUE"
const val DATE = "DATE"

const val DAILY = "DAILY"
const val WEEKLY = "WEEKLY"
const val MONTHLY = "MONTHLY"
const val YEARLY = "YEARLY"

const val MO = "MO"
const val TU = "TU"
const val WE = "WE"
const val TH = "TH"
const val FR = "FR"
const val SA = "SA"
const val SU = "SU"

// font sizes
const val FONT_SIZE_SMALL = 0
const val FONT_SIZE_MEDIUM = 1
const val FONT_SIZE_LARGE = 2

const val SOURCE_SIMPLE_CALENDAR = "simple-calendar"
const val SOURCE_IMPORTED_ICS = "imported-ics"
const val SOURCE_CONTACT_BIRTHDAY = "contact-birthday"
const val SOURCE_CONTACT_ANNIVERSARY = "contact-anniversary"

const val SOURCE_CUSTOMIZE_ANNIVERSARY = "customize-anniversary"

const val REMINDER_SWITCH ="reminder_switch"
const val REMINDER_INITIAL_MINUTES=240 //4 hours before REMINDER_UNIFIED_TIME_VALUE
const val SKCAL_AS_DEFAULT="skcal_as_default"
const val INTRO_TYPE="intro_type"
const val HEALTH_TITLE="health_title"
const val HEALTH_CONTENT="health_content"
const val HEALTH_CONTENT2="health_content2"

const val NOTIFICATION_ID="notification_id"
const val NOTIFICATION_TITLE="notification_title"
const val NOTIFICATION_CONTENT="notification_content"
const val NOTIFICATION_TS="notification_ts"
const val APP_TAG="SKCAL"
const val POSTPONE_TS=5*60*1000
const val TODAY_CODE = "today_code"

const val ANCESTOR =1
const val PARENTS= 2
const val RESPECTER= 3
const val SPOUSE= 4
const val YOURSELF= 5

const val BIRTH_DAY= 1
const val FORBIDDEN_DAY= 2
const val ANNIVERSARY_DAY= 3

const val PLACEHOLDER_8WHITESPACE="                "

const val SETTINGS_ACTIVITY_CLASSNAME="SettingsActivity"
const val ABOUT_ACTIVITY_CLASSNAME="AboutActivity"
const val MAIN_ACTIVITY_CLASSNAME="MainActivity"
