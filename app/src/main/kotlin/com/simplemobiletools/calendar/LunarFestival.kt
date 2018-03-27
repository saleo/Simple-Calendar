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

package com.simplemobiletools.calendar

import android.content.Context

//import com.android.internal.R;


object LunarFestival {

    fun getLunarFestival(chinadate: String, lunar: Lunar, context: Context): String {
        var chinadate = chinadate
        val lunarFestivalArray = context.resources.getStringArray(R.array.lunar_festival)
        chinadate = chinadate.substring(chinadate.length - 4, chinadate.length)
        for (i in lunarFestivalArray.indices) {
            val lunar_str = lunarFestivalArray[i].split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (lunar_str[0] == chinadate) {
                return if (i == 0) {
                    if (lunar.isBigMonth(lunar_str[0])) "" else lunar_str[1]
                } else {
                    lunar_str[1]
                }
            }
        }
        return ""
    }
}

