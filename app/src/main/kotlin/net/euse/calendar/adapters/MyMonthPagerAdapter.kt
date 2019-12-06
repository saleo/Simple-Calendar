package net.euse.calendar.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import android.util.SparseArray
import net.euse.calendar.fragments.MonthFragment
import net.euse.calendar.helpers.DAY_CODE
import net.euse.calendar.interfaces.NavigationListener

class MyMonthPagerAdapter(fm: androidx.fragment.app.FragmentManager, private val mCodes: List<String>, private val mListener: NavigationListener) : androidx.fragment.app.FragmentStatePagerAdapter(fm) {
    private val mFragments = SparseArray<MonthFragment>()

    override fun getCount() = mCodes.size

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        val bundle = Bundle()
        val code = mCodes[position]
        bundle.putString(DAY_CODE, code)

        val fragment = MonthFragment()
        fragment.arguments = bundle
        fragment.listener = mListener

        mFragments.put(position, fragment)

        return fragment
    }

    fun updateCalendars(pos: Int) {
        for (i in -1..1) {
            mFragments[pos + i]?.updateCalendar()
        }
    }
}
