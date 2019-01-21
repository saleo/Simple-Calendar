/*
 * Copyright (C) 2012 The MoKee Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.euse.calendar

import android.content.Context
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

//import com.android.internal.R;

class Lunar(private val mCalendar: Calendar, private val mContext: Context) {
    private val year: Int
    private val month: Int
    private val day: Int
    private var leap: Boolean = false
    private val chineseNumber: Array<String>
    private val lunarMonthName: Array<String>
    private val chineseDateFormat: SimpleDateFormat

    private fun yearDays(y: Int): Int {
        var i: Long
        var sum = 348
        i = 0x8000
        while (i > 0x8) {
            if (lunarInfo[y - 1900] and i != 0L)
                sum += 1
            i = i shr 1
        }
        return sum + leapDays(y)
    }

    private fun leapDays(y: Int): Int {
        return if (leapMonth(y) != 0) {
            if (lunarInfo[y - 1900] and 0x10000 != 0L)
                30
            else
                29
        } else
            0
    }

    private fun leapMonth(y: Int): Int {
        return (lunarInfo[y - 1900] and 0xf).toInt()
    }

    private fun monthDays(y: Int, m: Int): Int {
        return if (lunarInfo[y - 1900] and (0x10000 shr m).toLong() == 0L)
            29
        else
            30
    }

    fun animalsYear(): String {
        val Animals = mContext.resources.getStringArray(R.array.animals)
        return Animals[(year - 4) % 12]
    }

    private fun cyclicalm(num: Int): String {
        val Gan = mContext.resources.getStringArray(R.array.gan)
        val Zhi = mContext.resources.getStringArray(R.array.zhi)
        return Gan[num % 10] + Zhi[num % 12]
    }

    fun cyclical(): String {
        val num = year - 1900 + 36
        return cyclicalm(num)
    }

    init {
        val yearCyl: Int
        var monCyl: Int
        val dayCyl: Int
        var leapMonth = 0
        chineseNumber = mContext.resources.getStringArray(R.array.chinesenumber)
        lunarMonthName = mContext.resources.getStringArray(R.array.lunar_month_name)
        val format1 = mContext.resources.getString(R.string.status_format1)
        chineseDateFormat = SimpleDateFormat(format1)
        var baseDate: Date? = null
        try {
            val format2 = mContext.resources.getString(R.string.status_format2)
            baseDate = chineseDateFormat.parse(format2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        var offset = ((mCalendar.time.time - baseDate!!.time) / 86400000L).toInt()
        dayCyl = offset + 40
        monCyl = 14
        var iYear: Int
        var daysOfYear = 0
        iYear = 1900
        while (iYear < 2050 && offset > 0) {
            daysOfYear = yearDays(iYear)
            offset -= daysOfYear
            monCyl += 12
            iYear++
        }
        if (offset < 0) {
            offset += daysOfYear
            iYear--
            monCyl -= 12
        }
        year = iYear
        yearCyl = iYear - 1864
        leapMonth = leapMonth(iYear)
        leap = false
        var iMonth: Int
        var daysOfMonth = 0
        iMonth = 1
        while (iMonth < 13 && offset > 0) {
            if (leapMonth > 0 && iMonth == leapMonth + 1 && !leap) {
                --iMonth
                leap = true
                daysOfMonth = leapDays(year)
            } else
                daysOfMonth = monthDays(year, iMonth)
            offset -= daysOfMonth
            if (leap && iMonth == leapMonth + 1)
                leap = false
            if (!leap)
                monCyl++
            iMonth++
        }
        if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (leap) {
                leap = false
            } else {
                leap = true
                --iMonth
                --monCyl
            }
        }
        if (offset < 0) {
            offset += daysOfMonth
            --iMonth
            --monCyl
        }
        month = iMonth
        day = offset + 1
    }

    fun getChinaDayString(day: Int): String {
        val chineseTen = mContext.resources.getStringArray(R.array.chineseten)
        val n = if (day % 10 == 0) 9 else day % 10 - 1
        return if (day > 30)
            ""
        else if (day == 10)
            mContext.resources.getString(R.string.status_chushi)
        else if (day == 20)
            mContext.resources.getString(R.string.status_ershi)
        else if (day == 30)
            mContext.resources.getString(R.string.status_sanshi)
        else if (day==1)
            mContext.resources.getString(R.string.status_chuyi)
        else
            chineseTen[day / 10] + chineseNumber[n]
    }

    override fun toString(): String {
        val year1 = mContext.resources.getString(R.string.status_year)
        val run1 = mContext.resources.getString(R.string.status_leap)
        val month1 = mContext.resources.getString(R.string.status_month)
        return (cyclical() + animalsYear() + year1 + (if (leap) run1 else "") + lunarMonthName[month - 1]
                + getChinaDayString(day))
    }

    fun isBigMonth(lunarFestivalStr: String): Boolean {
        return monthDays(year, month) == 30
    }

    companion object {

        internal val lunarInfo = longArrayOf(0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6, 0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0)
    }
}

