package net.euse.skcal.interfaces

import android.util.SparseArray
import net.euse.skcal.models.DayYearly
import java.util.*

interface YearlyCalendar {
    fun updateYearlyCalendar(events: SparseArray<ArrayList<DayYearly>>, hashCode: Int)
}
